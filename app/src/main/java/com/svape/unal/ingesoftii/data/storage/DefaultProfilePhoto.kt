package com.svape.unal.ingesoftii.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.svape.unal.ingesoftii.domain.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

object DefaultProfilePhoto {

    private const val DEFAULT_PHOTOS_FOLDER = "perfiles/_default"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requests = ConcurrentHashMap<String, Deferred<String?>>()
    private val resolved = ConcurrentHashMap<String, String>()

    fun cached(role: String?): String? = fileNameFor(role)?.let { resolved[it] }

    suspend fun get(role: String?): String? {
        val fileName = fileNameFor(role) ?: return null
        resolved[fileName]?.let { return it }
        val request = requests.getOrPut(fileName) {
            scope.async {
                try {
                    FirebaseStorage.getInstance().reference
                        .child("$DEFAULT_PHOTOS_FOLDER/$fileName")
                        .downloadUrl
                        .await()
                        .toString()
                        .also { resolved[fileName] = it }
                } catch (e: Exception) {
                    // Nothing or error Storage
                    null
                }
            }
        }
        return request.await()
    }

    private fun fileNameFor(role: String?): String? {
        val normalized = role?.trim()?.lowercase() ?: return null
        return when (normalized) {
            in setOf("docente", UserRole.TEACHER.lowercase()) -> "docente.jpg"
            in setOf("estudiante", UserRole.STUDENT.lowercase()) -> "estudiante.jpg"
            else -> null
        }
    }
}