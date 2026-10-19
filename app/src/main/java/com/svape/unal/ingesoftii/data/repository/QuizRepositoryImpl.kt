package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.svape.unal.ingesoftii.domain.model.QuizAnswer
import com.svape.unal.ingesoftii.domain.model.QuizError
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.repository.QuizRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

private const val COURSES_COLLECTION = "cursos"
private const val QUESTION_BANK_SUBCOLLECTION = "bancoPreguntas"
private const val SESSIONS_SUBCOLLECTION = "sesionQuiz"
private const val ANSWERS_SUBCOLLECTION = "respuestas"
private const val PRIVATE_SUBCOLLECTION = "privado"
private const val ANSWER_KEY_DOCUMENT = "clave"

private const val QUESTION_TEXT_FIELD = "texto"
private const val OPTIONS_FIELD = "opciones"
private const val CORRECT_OPTION_FIELD = "indiceRespuestaCorrecta"
private const val CREATED_AT_FIELD = "creadaEn"

private const val SESSION_QUESTION_TEXT_FIELD = "preguntaTexto"
private const val SESSION_OPTIONS_FIELD = "opciones"
private const val SESSION_CORRECT_OPTION_FIELD = "indiceRespuestaCorrecta"
private const val DURATION_FIELD = "duracionSegundos"
private const val ACTIVE_FIELD = "activa"
private const val LAUNCHED_AT_FIELD = "lanzadaEn"
private const val EXPIRES_AT_FIELD = "expiraEn"

private const val STUDENT_NAME_FIELD = "nombreEstudiante"
private const val SELECTED_OPTION_FIELD = "indiceSeleccionado"
private const val RESPONSE_TIME_FIELD = "tiempoRespuestaMs"
private const val SCORE_FIELD = "puntaje"
private const val ANSWERED_AT_FIELD = "respondidoEn"
private const val IS_CORRECT_FIELD = "esCorrecta"

class QuizRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : QuizRepository {

    private val answerKeys = ConcurrentHashMap<String, Int>()

    override suspend fun getQuestionBank(courseId: String): Result<List<QuizQuestion>> {
        return try {
            val snapshot = bankCollection(courseId)
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.documents.map { it.toQuizQuestion() })
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                QuizError.Unknown(
                    e.message ?: "No se pudo cargar el banco de preguntas"
                )
            )
        }
    }

    override suspend fun createQuestion(
        courseId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ): Result<QuizQuestion> {
        return try {
            val docRef = bankCollection(courseId).document()
            val now = Timestamp.now()
            docRef.set(
                mapOf(
                    QUESTION_TEXT_FIELD to text,
                    OPTIONS_FIELD to options,
                    CORRECT_OPTION_FIELD to correctOptionIndex,
                    CREATED_AT_FIELD to now
                )
            ).await()
            Result.success(
                QuizQuestion(docRef.id, text, options, correctOptionIndex, now.toDate().time)
            )
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo crear la pregunta"))
        }
    }

    override suspend fun updateQuestion(
        courseId: String,
        questionId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ): Result<Unit> {
        return try {
            bankCollection(courseId).document(questionId)
                .update(
                    mapOf(
                        QUESTION_TEXT_FIELD to text,
                        OPTIONS_FIELD to options,
                        CORRECT_OPTION_FIELD to correctOptionIndex
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo editar la pregunta"))
        }
    }

    override suspend fun deleteQuestion(courseId: String, questionId: String): Result<Unit> {
        return try {
            bankCollection(courseId).document(questionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo eliminar la pregunta"))
        }
    }

    //Live session (teacher)

    override suspend fun launchQuestion(
        courseId: String,
        question: QuizQuestion,
        durationSeconds: Int
    ): Result<QuizSession> {
        return try {
            val sessionsRef = sessionsCollection(courseId)

            val activeSnapshot = sessionsRef.whereEqualTo(ACTIVE_FIELD, true).get().await()
            val closingUpdates = activeSnapshot.documents.map { closingUpdate(courseId, it.id) }
            val batch = firestore.batch()
            activeSnapshot.documents.forEachIndexed { index, document ->
                batch.update(document.reference, closingUpdates[index])
            }

            val newDocRef = sessionsRef.document()
            val now = Timestamp.now()
            val expiresAt = Timestamp(java.util.Date(now.toDate().time + durationSeconds * 1000L))

            batch.set(
                newDocRef,
                mapOf(
                    SESSION_QUESTION_TEXT_FIELD to question.text,
                    SESSION_OPTIONS_FIELD to question.options,
                    DURATION_FIELD to durationSeconds,
                    ACTIVE_FIELD to true,
                    LAUNCHED_AT_FIELD to now,
                    EXPIRES_AT_FIELD to expiresAt
                )
            )
            batch.set(
                answerKeyDocument(courseId, newDocRef.id),
                mapOf(SESSION_CORRECT_OPTION_FIELD to question.correctOptionIndex)
            )
            answerKeys[newDocRef.id] = question.correctOptionIndex
            batch.commit().await()

            Result.success(
                QuizSession(
                    id = newDocRef.id,
                    questionText = question.text,
                    options = question.options,
                    correctOptionIndex = question.correctOptionIndex,
                    durationSeconds = durationSeconds,
                    isActive = true,
                    launchedAt = now.toDate().time,
                    expiresAt = expiresAt.toDate().time
                )
            )
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo lanzar la pregunta"))
        }
    }

    override suspend fun closeSession(courseId: String, sessionId: String): Result<Unit> {
        return try {
            sessionsCollection(courseId).document(sessionId)
                .update(closingUpdate(courseId, sessionId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo cerrar la pregunta"))
        }
    }

    //Shared

    override fun observeLatestSession(
        courseId: String,
        withAnswerKey: Boolean
    ): Flow<QuizSession?> = callbackFlow {
        var latest: QuizSession? = null

        fun publish(session: QuizSession?) {
            latest = session
            trySend(session?.withKnownAnswerKey())
        }

        val listener = sessionsCollection(courseId)
            .orderBy(LAUNCHED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                val session = snapshot?.documents?.firstOrNull()?.toQuizSession()
                publish(session)

                if (withAnswerKey && session != null && session.correctOptionIndex == null &&
                    !answerKeys.containsKey(session.id)
                ) {
                    answerKeyDocument(courseId, session.id).get().addOnSuccessListener { key ->
                        val correctOption = key.getLong(SESSION_CORRECT_OPTION_FIELD)?.toInt()
                        if (correctOption != null) {
                            answerKeys[session.id] = correctOption
                            if (latest?.id == session.id) publish(latest)
                        }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override fun observeSessionAnswers(
        courseId: String,
        sessionId: String
    ): Flow<List<QuizAnswer>> = callbackFlow {
        val listener = answersCollection(courseId, sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map { it.toQuizAnswer() })
            }
        awaitClose { listener.remove() }
    }

    //Student

    override suspend fun submitAnswer(
        courseId: String,
        session: QuizSession,
        selectedOptionIndex: Int
    ): Result<Unit> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(QuizError.Unknown("Usuario no autenticado"))

        return try {
            val ownProfile = firestore.collection("usuarios").document(studentUid).get().await()
            val studentName = ownProfile.getString("nombre")?.takeIf { it.isNotBlank() }
                ?: auth.currentUser?.email
                ?: studentUid

            answersCollection(courseId, session.id).document(studentUid)
                .set(
                    mapOf(
                        STUDENT_NAME_FIELD to studentName,
                        SELECTED_OPTION_FIELD to selectedOptionIndex,
                        ANSWERED_AT_FIELD to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(QuizError.Unknown(e.message ?: "No se pudo registrar tu respuesta"))
        }
    }

    override fun observeMyAnswer(courseId: String, sessionId: String): Flow<QuizAnswer?> =
        callbackFlow {
            val studentUid = auth.currentUser?.uid
            if (studentUid == null) {
                close(QuizError.Unknown("Usuario no autenticado"))
                return@callbackFlow
            }
            val listener = answersCollection(courseId, sessionId).document(studentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        crashReporter.recordException(error)
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.takeIf { it.exists() }?.toQuizAnswer())
                }
            awaitClose { listener.remove() }
        }

    //Helpers

    private suspend fun closingUpdate(courseId: String, sessionId: String): Map<String, Any> {
        val update = mutableMapOf<String, Any>(ACTIVE_FIELD to false)
        val correctOption = answerKeyDocument(courseId, sessionId).get().await()
            .getLong(SESSION_CORRECT_OPTION_FIELD)?.toInt()
        if (correctOption != null) update[SESSION_CORRECT_OPTION_FIELD] = correctOption
        return update
    }

    private fun QuizSession.withKnownAnswerKey(): QuizSession =
        if (correctOptionIndex != null) this else copy(correctOptionIndex = answerKeys[id])

    private fun answerKeyDocument(courseId: String, sessionId: String) =
        sessionsCollection(courseId).document(sessionId)
            .collection(PRIVATE_SUBCOLLECTION).document(ANSWER_KEY_DOCUMENT)

    private fun bankCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId)
            .collection(QUESTION_BANK_SUBCOLLECTION)

    private fun sessionsCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId)
            .collection(SESSIONS_SUBCOLLECTION)

    private fun answersCollection(courseId: String, sessionId: String) =
        sessionsCollection(courseId).document(sessionId).collection(ANSWERS_SUBCOLLECTION)

    private fun DocumentSnapshot.toQuizQuestion(): QuizQuestion {
        val optionsRaw = get(OPTIONS_FIELD) as? List<*> ?: emptyList<Any>()
        return QuizQuestion(
            id = id,
            text = getString(QUESTION_TEXT_FIELD).orEmpty(),
            options = optionsRaw.filterIsInstance<String>(),
            correctOptionIndex = getLong(CORRECT_OPTION_FIELD)?.toInt() ?: 0,
            createdAt = getTimestamp(CREATED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toQuizSession(): QuizSession {
        val optionsRaw = get(SESSION_OPTIONS_FIELD) as? List<*> ?: emptyList<Any>()
        return QuizSession(
            id = id,
            questionText = getString(SESSION_QUESTION_TEXT_FIELD).orEmpty(),
            options = optionsRaw.filterIsInstance<String>(),
            correctOptionIndex = getLong(SESSION_CORRECT_OPTION_FIELD)?.toInt(),
            durationSeconds = getLong(DURATION_FIELD)?.toInt() ?: 0,
            isActive = getBoolean(ACTIVE_FIELD) ?: false,
            launchedAt = getTimestamp(LAUNCHED_AT_FIELD)?.toDate()?.time ?: 0L,
            expiresAt = getTimestamp(EXPIRES_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toQuizAnswer(): QuizAnswer {
        return QuizAnswer(
            studentUid = id,
            studentName = getString(STUDENT_NAME_FIELD).orEmpty().ifBlank { id },
            selectedOptionIndex = getLong(SELECTED_OPTION_FIELD)?.toInt() ?: -1,
            responseTimeMillis = getLong(RESPONSE_TIME_FIELD) ?: 0L,
            score = getLong(SCORE_FIELD)?.toInt() ?: 0,
            answeredAt = getTimestamp(ANSWERED_AT_FIELD)?.toDate()?.time ?: 0L,
            isCorrect = getBoolean(IS_CORRECT_FIELD)
        )
    }
}
