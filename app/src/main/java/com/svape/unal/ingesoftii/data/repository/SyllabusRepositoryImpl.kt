package com.svape.unal.ingesoftii.data.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.svape.unal.ingesoftii.domain.model.SyllabusError
import com.svape.unal.ingesoftii.domain.repository.SyllabusRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.tasks.await
import java.io.File

private const val COURSES_COLLECTION = "cursos"
private const val SYLLABUS_PATH_FIELD = "syllabusPdfPath"
private const val LOCAL_FILE_NAME = "syllabus.pdf"

class SyllabusRepositoryImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val crashReporter: CrashReporter
) : SyllabusRepository {

    override suspend fun downloadSyllabus(courseId: String): Result<File> {
        return try {
            val courseDoc =
                firestore.collection(COURSES_COLLECTION).document(courseId).get().await()
            val storagePath = courseDoc.getString(SYLLABUS_PATH_FIELD)

            if (storagePath.isNullOrBlank()) {
                return Result.failure(SyllabusError.NotAvailable)
            }

            val localFile = File(context.cacheDir, LOCAL_FILE_NAME)
            val storageRef = storage.reference.child(storagePath)
            storageRef.getFile(localFile).await()

            Result.success(localFile)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(SyllabusError.Unknown(e.message ?: "Error al cargar el Syllabus"))
        }
    }
}