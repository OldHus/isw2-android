package com.svape.unal.ingesoftii.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.svape.unal.ingesoftii.domain.model.CourseError
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.RegistrationError
import com.svape.unal.ingesoftii.domain.model.RegistrationResult
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.repository.CourseRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "usuarios"
private const val ACCESS_CODES_COLLECTION = "codigosAcceso"
private const val ACCESS_CODE_FIELD = "codigoAcceso"
private const val ROLE_FIELD = "rol"
private const val COURSE_ID_FIELD = "cursoId"
private const val NAME_FIELD = "nombre"
private const val EMAIL_FIELD = "correo"
private const val PHOTO_URL_FIELD = "fotoUrl"

class CourseRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val crashReporter: CrashReporter
) : CourseRepository {

    override suspend fun redeemRegistrationCode(code: String): Result<RegistrationResult> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(RegistrationError.UserNotAuthenticated)

            val normalizedCode = code.trim()
            if (normalizedCode.isEmpty() || normalizedCode.contains("/")) {
                return Result.failure(RegistrationError.InvalidCode)
            }

            val codeDocument = firestore.collection(ACCESS_CODES_COLLECTION)
                .document(normalizedCode)
                .get()
                .await()

            val courseId = codeDocument.getString(COURSE_ID_FIELD)?.takeIf { it.isNotBlank() }
                ?: return Result.failure(RegistrationError.InvalidCode)

            val userProfile = mapOf(
                NAME_FIELD to (currentUser.displayName ?: ""),
                EMAIL_FIELD to (currentUser.email ?: ""),
                PHOTO_URL_FIELD to (currentUser.photoUrl?.toString() ?: ""),
                ROLE_FIELD to UserRole.STUDENT,
                COURSE_ID_FIELD to courseId,
                ACCESS_CODE_FIELD to normalizedCode
            )

            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .set(userProfile)
                .await()

            Result.success(RegistrationResult.Success(UserRole.STUDENT, courseId))
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                RegistrationError.Unknown(
                    e.message ?: "Unknown error while redeeming the code"
                )
            )
        }
    }

    override suspend fun getCourseStudents(courseId: String): Result<List<CourseStudent>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo(COURSE_ID_FIELD, courseId)
                .whereEqualTo(ROLE_FIELD, UserRole.STUDENT)
                .get()
                .await()

            val students = snapshot.documents.map { doc ->
                CourseStudent(
                    uid = doc.id,
                    name = doc.getString(NAME_FIELD).orEmpty(),
                    email = doc.getString(EMAIL_FIELD).orEmpty(),
                    photoUrl = doc.getString(PHOTO_URL_FIELD).orEmpty()
                )
            }
            Result.success(students)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(
                CourseError.Unknown(
                    e.message ?: "No se pudo cargar la lista de estudiantes"
                )
            )
        }
    }
}