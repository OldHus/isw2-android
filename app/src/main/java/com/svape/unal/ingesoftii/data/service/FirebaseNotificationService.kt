package com.svape.unal.ingesoftii.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import com.svape.unal.ingesoftii.domain.service.NotificationService
import kotlinx.coroutines.tasks.await

private const val TAG = "IngesoftFCM"

private fun topicFor(courseId: String, role: String): String {
    val suffix = if (role == UserRole.TEACHER) "docente" else "estudiantes"
    return "curso_${courseId}_$suffix"
}

class FirebaseNotificationService(
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
    private val crashReporter: CrashReporter
) : NotificationService {

    override suspend fun subscribeToCourseTopic(courseId: String, role: String): Result<Unit> {
        val topic = topicFor(courseId, role)
        Log.d(TAG, "Intentando suscribirse al tema: $topic")
        return try {
            messaging.subscribeToTopic(topic).await()
            Log.d(TAG, "✅ Suscripción EXITOSA al tema: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ FALLO al suscribirse al tema: $topic", e)
            crashReporter.recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun unsubscribeFromCourseTopic(courseId: String, role: String): Result<Unit> {
        val topic = topicFor(courseId, role)
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Desuscripción exitosa del tema: $topic")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al desuscribirse del tema: $topic", e)
            crashReporter.recordException(e)
            Result.failure(e)
        }
    }
}