package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.model.PollError
import com.svape.unal.ingesoftii.domain.model.PollVote
import com.svape.unal.ingesoftii.domain.repository.PollRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "cursos"
private const val POLLS_SUBCOLLECTION = "encuestas"
private const val VOTES_SUBCOLLECTION = "votos"
private const val STUDENT_NAME_FIELD = "nombreEstudiante"

private const val QUESTION_FIELD = "pregunta"
private const val OPTIONS_FIELD = "opciones"
private const val ACTIVE_FIELD = "activa"
private const val DURATION_FIELD = "duracionMinutos"
private const val CREATED_AT_FIELD = "creadaEn"
private const val EXPIRES_AT_FIELD = "expiraEn"
private const val OPTION_INDEX_FIELD = "opcionIndex"
private const val VOTED_AT_FIELD = "votadoEn"

class PollRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : PollRepository {

    //Teacher

    override suspend fun startPoll(
        courseId: String,
        question: String,
        options: List<String>,
        expiresAtMillis: Long
    ): Result<Poll> {
        return try {
            val pollsRef = pollsCollection(courseId)

            val activeSnapshot = pollsRef.whereEqualTo(ACTIVE_FIELD, true).get().await()
            val batch = firestore.batch()
            activeSnapshot.documents.forEach { batch.update(it.reference, ACTIVE_FIELD, false) }

            val newDocRef = pollsRef.document()
            val now = Timestamp.now()
            val expiresAt = Timestamp(java.util.Date(expiresAtMillis))

            val data = mapOf(
                QUESTION_FIELD to question,
                OPTIONS_FIELD to options,
                ACTIVE_FIELD to true,
                CREATED_AT_FIELD to now,
                EXPIRES_AT_FIELD to expiresAt
            )
            batch.set(newDocRef, data)
            batch.commit().await()

            Result.success(
                Poll(
                    id = newDocRef.id,
                    question = question,
                    options = options,
                    isActive = true,
                    createdAt = now.toDate().time,
                    expiresAt = expiresAt.toDate().time
                )
            )
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PollError.Unknown(e.message ?: "No se pudo crear la encuesta"))
        }
    }

    override suspend fun closePoll(courseId: String, pollId: String): Result<Unit> {
        return try {
            pollsCollection(courseId).document(pollId)
                .update(ACTIVE_FIELD, false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PollError.Unknown(e.message ?: "No se pudo cerrar la encuesta"))
        }
    }

    //Shared

    override fun observeLatestPoll(courseId: String): Flow<Poll?> = callbackFlow {
        val listener = pollsCollection(courseId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.firstOrNull()?.toPoll())
            }
        awaitClose { listener.remove() }
    }

    override fun observePollVotes(courseId: String, pollId: String): Flow<List<PollVote>> =
        callbackFlow {
            val listener = votesCollection(courseId, pollId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        crashReporter.recordException(error)
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents.orEmpty().map { it.toPollVote() })
                }
            awaitClose { listener.remove() }
        }

    // Student

    override suspend fun submitVote(
        courseId: String,
        pollId: String,
        optionIndex: Int
    ): Result<Unit> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(PollError.Unknown("Usuario no autenticado"))

        return try {
            val ownProfile = firestore.collection("usuarios").document(studentUid).get().await()
            val studentName = ownProfile.getString("nombre")?.takeIf { it.isNotBlank() }
                ?: auth.currentUser?.email
                ?: studentUid

            votesCollection(courseId, pollId).document(studentUid)
                .set(
                    mapOf(
                        OPTION_INDEX_FIELD to optionIndex,
                        VOTED_AT_FIELD to Timestamp.now(),
                        STUDENT_NAME_FIELD to studentName
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PollError.Unknown(e.message ?: "No se pudo registrar tu voto"))
        }
    }

    override fun observeMyVote(courseId: String, pollId: String): Flow<PollVote?> = callbackFlow {
        val studentUid = auth.currentUser?.uid
        if (studentUid == null) {
            close(PollError.Unknown("Usuario no autenticado"))
            return@callbackFlow
        }
        val listener = votesCollection(courseId, pollId).document(studentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.takeIf { it.exists() }?.toPollVote())
            }
        awaitClose { listener.remove() }
    }

    //Helpers

    private fun pollsCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId).collection(POLLS_SUBCOLLECTION)

    private fun votesCollection(courseId: String, pollId: String) =
        pollsCollection(courseId).document(pollId).collection(VOTES_SUBCOLLECTION)

    private fun DocumentSnapshot.toPoll(): Poll {
        val optionsRaw = get(OPTIONS_FIELD) as? List<*> ?: emptyList<Any>()
        return Poll(
            id = id,
            question = getString(QUESTION_FIELD).orEmpty(),
            options = optionsRaw.filterIsInstance<String>(),
            isActive = getBoolean(ACTIVE_FIELD) ?: false,
            createdAt = getTimestamp(CREATED_AT_FIELD)?.toDate()?.time ?: 0L,
            expiresAt = getTimestamp(EXPIRES_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toPollVote(): PollVote {
        return PollVote(
            studentUid = id,
            studentName = getString(STUDENT_NAME_FIELD).orEmpty().ifBlank { id },
            optionIndex = getLong(OPTION_INDEX_FIELD)?.toInt() ?: -1,
            votedAt = getTimestamp(VOTED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }
}