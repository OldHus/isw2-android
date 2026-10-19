package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.svape.unal.ingesoftii.domain.model.AttendanceError
import com.svape.unal.ingesoftii.domain.model.AttendanceRecord
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

private const val COURSES_COLLECTION = "cursos"
private const val SESSIONS_SUBCOLLECTION = "sesionesAsistencia"
private const val REGISTRATIONS_SUBCOLLECTION = "registros"
private const val PRIVATE_SUBCOLLECTION = "privado"
private const val CODE_DOCUMENT = "clave"

private const val CODE_FIELD = "codigo"
private const val ACTIVE_FIELD = "activa"
private const val DURATION_FIELD = "duracionMinutos"
private const val CREATED_AT_FIELD = "creadaEn"
private const val EXPIRES_AT_FIELD = "expiraEn"
private const val REGISTERED_AT_FIELD = "registradoEn"

class AttendanceRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : AttendanceRepository {

    private val sessionCodes = ConcurrentHashMap<String, String>()

    //Teacher

    override suspend fun startSession(
        courseId: String,
        code: String,
        durationMinutes: Int
    ): Result<AttendanceSession> {
        return try {
            val sessionsRef = sessionsCollection(courseId)
            val activeSnapshot = sessionsRef.whereEqualTo(ACTIVE_FIELD, true).get().await()
            val batch = firestore.batch()
            activeSnapshot.documents.forEach { batch.update(it.reference, ACTIVE_FIELD, false) }

            val newDocRef = sessionsRef.document()
            val now = Timestamp.now()
            val expiresAt = Timestamp(now.seconds + durationMinutes * 60L, now.nanoseconds)

            batch.set(
                newDocRef,
                mapOf(
                    ACTIVE_FIELD to true,
                    DURATION_FIELD to durationMinutes,
                    CREATED_AT_FIELD to now,
                    EXPIRES_AT_FIELD to expiresAt
                )
            )
            batch.set(codeDocument(courseId, newDocRef.id), mapOf(CODE_FIELD to code))
            sessionCodes[newDocRef.id] = code
            batch.commit().await()

            Result.success(
                AttendanceSession(
                    id = newDocRef.id,
                    code = code,
                    isActive = true,
                    durationMinutes = durationMinutes,
                    createdAt = now.toDate().time,
                    expiresAt = expiresAt.toDate().time
                )
            )
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                AttendanceError.Unknown(
                    e.message ?: "No se pudo iniciar la sesión de asistencia"
                )
            )
        }
    }

    override suspend fun closeSession(courseId: String, sessionId: String): Result<Unit> {
        return try {
            sessionsCollection(courseId).document(sessionId)
                .update(ACTIVE_FIELD, false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(AttendanceError.Unknown(e.message ?: "No se pudo cerrar la sesión"))
        }
    }

    override fun observeSessionRegistrations(
        courseId: String,
        sessionId: String
    ): Flow<List<AttendanceRecord>> = callbackFlow {
        val listener = registrationsCollection(courseId, sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map { it.toAttendanceRecord() })
            }
        awaitClose { listener.remove() }
    }

    //Shared

    override suspend fun getActiveSession(courseId: String): Result<AttendanceSession?> {
        return try {
            val snapshot = sessionsCollection(courseId)
                .whereEqualTo(ACTIVE_FIELD, true)
                .limit(1)
                .get()
                .await()
            Result.success(snapshot.documents.firstOrNull()?.toAttendanceSession())
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                AttendanceError.Unknown(
                    e.message ?: "No se pudo obtener la sesión activa"
                )
            )
        }
    }

    override fun observeActiveSession(
        courseId: String,
        includeCode: Boolean
    ): Flow<AttendanceSession?> = callbackFlow {
        var latestSessionId: String? = null

        val listener = sessionsCollection(courseId)
            .whereEqualTo(ACTIVE_FIELD, true)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                val session = snapshot?.documents?.firstOrNull()?.toAttendanceSession()
                latestSessionId = session?.id

                if (session == null || !includeCode || session.code.isNotEmpty()) {
                    trySend(session)
                    return@addSnapshotListener
                }

                val cachedCode = sessionCodes[session.id]
                if (cachedCode != null) {
                    trySend(session.copy(code = cachedCode))
                    return@addSnapshotListener
                }

                codeDocument(courseId, session.id).get()
                    .addOnSuccessListener { document ->
                        val code = document.getString(CODE_FIELD).orEmpty()
                        if (code.isNotEmpty()) sessionCodes[session.id] = code
                        if (latestSessionId == session.id) trySend(session.copy(code = code))
                    }
                    .addOnFailureListener { e ->
                        crashReporter.recordException(e)
                        if (latestSessionId == session.id) trySend(session)
                    }
            }
        awaitClose { listener.remove() }
    }

    //Student

    override suspend fun submitAttendance(
        courseId: String,
        sessionId: String,
        code: String
    ): Result<Unit> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(AttendanceError.Unknown("Usuario no autenticado"))

        return try {
            val registrationRef = registrationsCollection(courseId, sessionId).document(studentUid)

            if (registrationRef.get().await().exists()) {
                return Result.failure(AttendanceError.AlreadyRegistered)
            }

            registrationRef.set(
                mapOf(
                    REGISTERED_AT_FIELD to Timestamp.now(),
                    CODE_FIELD to code
                )
            ).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Result.failure(AttendanceError.InvalidCode)
            } else {
                crashReporter.recordException(e)
                Result.failure(
                    AttendanceError.Unknown(
                        e.message ?: "No se pudo registrar tu asistencia"
                    )
                )
            }
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                AttendanceError.Unknown(
                    e.message ?: "No se pudo registrar tu asistencia"
                )
            )
        }
    }

    override suspend fun hasSubmittedAttendance(
        courseId: String,
        sessionId: String
    ): Result<Boolean> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(AttendanceError.Unknown("Usuario no autenticado"))

        return try {
            val doc =
                registrationsCollection(courseId, sessionId).document(studentUid).get().await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                AttendanceError.Unknown(
                    e.message ?: "No se pudo verificar tu asistencia"
                )
            )
        }
    }

    //Helpers

    private fun sessionsCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId)
            .collection(SESSIONS_SUBCOLLECTION)

    private fun registrationsCollection(courseId: String, sessionId: String) =
        sessionsCollection(courseId).document(sessionId).collection(REGISTRATIONS_SUBCOLLECTION)

    private fun codeDocument(courseId: String, sessionId: String) =
        sessionsCollection(courseId).document(sessionId)
            .collection(PRIVATE_SUBCOLLECTION).document(CODE_DOCUMENT)

    private fun DocumentSnapshot.toAttendanceSession(): AttendanceSession {
        return AttendanceSession(
            id = id,
            code = getString(CODE_FIELD).orEmpty(),
            isActive = getBoolean(ACTIVE_FIELD) ?: false,
            durationMinutes = getLong(DURATION_FIELD)?.toInt() ?: 0,
            createdAt = getTimestamp(CREATED_AT_FIELD)?.toDate()?.time ?: 0L,
            expiresAt = getTimestamp(EXPIRES_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }

    private fun DocumentSnapshot.toAttendanceRecord(): AttendanceRecord {
        return AttendanceRecord(
            studentUid = id,
            registeredAt = getTimestamp(REGISTERED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }
}