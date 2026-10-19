package com.svape.unal.ingesoftii.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.domain.repository.QuizStandingRepository
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

private const val COURSES_COLLECTION = "cursos"
private const val SESSIONS_SUBCOLLECTION = "sesionQuiz"
private const val ANSWERS_SUBCOLLECTION = "respuestas"
private const val PRIVATE_SUBCOLLECTION = "privado"
private const val ANSWER_KEY_DOCUMENT = "clave"
private const val STUDENT_NAME_FIELD = "nombreEstudiante"
private const val SCORE_FIELD = "puntaje"
private const val ANSWERED_AT_FIELD = "respondidoEn"

class QuizStandingRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : QuizStandingRepository {

    override fun observeCourseStanding(courseId: String): Flow<List<QuizStandingEntry>> =
        callbackFlow {
            val currentUid = auth.currentUser?.uid
            val observedSessions = mutableSetOf<String>()
            val answerRegistrations = mutableMapOf<String, ListenerRegistration>()
            val scoresBySession = mutableMapOf<String, List<AnswerScore>>()
            val deniedSessions = mutableSetOf<String>()

            fun emitIfReady() {
                if (scoresBySession.size < observedSessions.size) return
                trySend(buildStanding(scoresBySession.values, currentUid))
            }

            fun stopSession(sessionId: String) {
                observedSessions.remove(sessionId)
                answerRegistrations.remove(sessionId)?.remove()
                scoresBySession.remove(sessionId)
                deniedSessions.remove(sessionId)
            }

            val sessionsRegistration =
                sessionsCollection(courseId).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val sessionIds = snapshot?.documents.orEmpty().map { it.id }.toSet()

                    (observedSessions - sessionIds).forEach { stopSession(it) }

                    deniedSessions.toList().forEach { stopSession(it) }

                    (sessionIds - observedSessions).forEach { sessionId ->
                        observedSessions.add(sessionId)
                        answerRegistrations[sessionId] = answersCollection(courseId, sessionId)
                            .addSnapshotListener { answersSnapshot, answersError ->
                                if (sessionId !in observedSessions) return@addSnapshotListener
                                if (answersError != null) {
                                    if (answersError.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                        close(answersError)
                                        return@addSnapshotListener
                                    }
                                    deniedSessions.add(sessionId)
                                    scoresBySession[sessionId] = emptyList()
                                    emitIfReady()
                                    return@addSnapshotListener
                                }
                                scoresBySession[sessionId] =
                                    answersSnapshot?.documents.orEmpty().map { it.toAnswerScore() }
                                emitIfReady()
                            }
                    }

                    emitIfReady()
                }

            awaitClose {
                sessionsRegistration.remove()
                answerRegistrations.values.forEach { it.remove() }
            }
        }

    override suspend fun resetCourseStanding(courseId: String): Result<Unit> {
        return try {
            val sessions = sessionsCollection(courseId).get().awaitResult().documents
            val answers = sessions.flatMap { session ->
                session.reference.collection(ANSWERS_SUBCOLLECTION).get().awaitResult().documents
            }
            Tasks.whenAll(answers.map { it.reference.delete() }).awaitResult()
            Tasks.whenAll(
                sessions.map {
                    it.reference.collection(PRIVATE_SUBCOLLECTION).document(ANSWER_KEY_DOCUMENT)
                        .delete()
                }
            ).awaitResult()
            Tasks.whenAll(sessions.map { it.reference.delete() }).awaitResult()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sessionsCollection(courseId: String): CollectionReference =
        firestore.collection(COURSES_COLLECTION).document(courseId)
            .collection(SESSIONS_SUBCOLLECTION)

    private fun answersCollection(courseId: String, sessionId: String): CollectionReference =
        sessionsCollection(courseId).document(sessionId).collection(ANSWERS_SUBCOLLECTION)
}

private data class AnswerScore(
    val studentUid: String,
    val studentName: String,
    val score: Int,
    val answeredAtMillis: Long
)

private fun DocumentSnapshot.toAnswerScore(): AnswerScore = AnswerScore(
    studentUid = id,
    studentName = getString(STUDENT_NAME_FIELD)?.trim().orEmpty(),
    score = getLong(SCORE_FIELD)?.toInt() ?: 0,
    answeredAtMillis = getDate(ANSWERED_AT_FIELD)?.time ?: 0L
)

private fun buildStanding(
    scoresBySession: Collection<List<AnswerScore>>,
    currentUid: String?
): List<QuizStandingEntry> =
    scoresBySession.flatten()
        .groupBy { it.studentUid }
        .map { (studentUid, answers) ->
            QuizStandingEntry(
                studentUid = studentUid,
                studentName = answers.maxBy { it.answeredAtMillis }.studentName,
                totalScore = answers.sumOf { it.score },
                isCurrentUser = studentUid == currentUid
            )
        }
        .sortedWith(
            compareByDescending<QuizStandingEntry> { it.totalScore }.thenBy { it.studentName.lowercase() }
        )

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        val exception = task.exception
        when {
            exception != null -> continuation.resumeWithException(exception)
            task.isCanceled -> continuation.cancel()
            else -> continuation.resume(task.result)
        }
    }
}
