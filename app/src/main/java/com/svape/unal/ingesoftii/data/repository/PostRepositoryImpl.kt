package com.svape.unal.ingesoftii.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.PostError
import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.repository.PostRepository
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COURSES_COLLECTION = "cursos"
private const val POSTS_SUBCOLLECTION = "posts"
private const val REACTIONS_SUBCOLLECTION = "reacciones"

private const val TEXT_FIELD = "texto"
private const val IMAGE_URL_FIELD = "imagenUrl"
private const val CREATED_AT_FIELD = "creadaEn"
private const val EDITED_AT_FIELD = "editadaEn"

private const val STUDENT_NAME_FIELD = "nombreEstudiante"
private const val REACTION_TYPE_FIELD = "tipo"
private const val REACTED_AT_FIELD = "reaccionadoEn"

class PostRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val crashReporter: CrashReporter
) : PostRepository {

    override suspend fun createPost(courseId: String, text: String, imageUri: Uri?): Result<Post> {
        return try {
            val docRef = postsCollection(courseId).document()
            val now = Timestamp.now()

            docRef.set(
                mapOf(
                    TEXT_FIELD to text,
                    IMAGE_URL_FIELD to null,
                    CREATED_AT_FIELD to now,
                    EDITED_AT_FIELD to null
                )
            ).await()

            val imageUrl = imageUri?.let { uploadImage(courseId, docRef.id, it) }
            if (imageUrl != null) {
                docRef.update(IMAGE_URL_FIELD, imageUrl).await()
            }

            Result.success(Post(docRef.id, text, imageUrl, now.toDate().time, null))
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PostError.Unknown(e.message ?: "No se pudo publicar"))
        }
    }

    override suspend fun updatePost(
        courseId: String,
        postId: String,
        text: String,
        newImageUri: Uri?,
        removeImage: Boolean
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>(
                TEXT_FIELD to text,
                EDITED_AT_FIELD to Timestamp.now()
            )
            when {
                newImageUri != null -> updates[IMAGE_URL_FIELD] =
                    uploadImage(courseId, postId, newImageUri)

                removeImage -> {
                    deleteImageIfExists(courseId, postId)
                    updates[IMAGE_URL_FIELD] = null
                }
            }
            postsCollection(courseId).document(postId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PostError.Unknown(e.message ?: "No se pudo editar la publicación"))
        }
    }

    override suspend fun deletePost(courseId: String, postId: String): Result<Unit> {
        return try {
            deleteImageIfExists(courseId, postId)
            postsCollection(courseId).document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PostError.Unknown(e.message ?: "No se pudo eliminar la publicación"))
        }
    }

    override fun observePosts(courseId: String): Flow<List<Post>> = callbackFlow {
        val listener = postsCollection(courseId)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    crashReporter.recordException(error)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map { it.toPost() })
            }
        awaitClose { listener.remove() }
    }

    override fun observePostReactions(courseId: String, postId: String): Flow<List<PostReaction>> =
        callbackFlow {
            val listener = reactionsCollection(courseId, postId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        crashReporter.recordException(error)
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents.orEmpty().map { it.toPostReaction() })
                }
            awaitClose { listener.remove() }
        }

    override suspend fun setMyReaction(
        courseId: String,
        postId: String,
        type: String
    ): Result<Unit> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(PostError.Unknown("Usuario no autenticado"))
        return try {
            val ownProfile = firestore.collection("usuarios").document(studentUid).get().await()
            val studentName = ownProfile.getString("nombre")?.takeIf { it.isNotBlank() }
                ?: auth.currentUser?.email
                ?: studentUid

            reactionsCollection(courseId, postId).document(studentUid)
                .set(
                    mapOf(
                        STUDENT_NAME_FIELD to studentName,
                        REACTION_TYPE_FIELD to type,
                        REACTED_AT_FIELD to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PostError.Unknown(e.message ?: "No se pudo registrar tu reacción"))
        }
    }

    override suspend fun removeMyReaction(courseId: String, postId: String): Result<Unit> {
        val studentUid = auth.currentUser?.uid
            ?: return Result.failure(PostError.Unknown("Usuario no autenticado"))
        return try {
            reactionsCollection(courseId, postId).document(studentUid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(PostError.Unknown(e.message ?: "No se pudo quitar tu reacción"))
        }
    }

    override fun observeMyReaction(courseId: String, postId: String): Flow<PostReaction?> =
        callbackFlow {
            val studentUid = auth.currentUser?.uid
            if (studentUid == null) {
                close(PostError.Unknown("Usuario no autenticado"))
                return@callbackFlow
            }
            val listener = reactionsCollection(courseId, postId).document(studentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        crashReporter.recordException(error)
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.takeIf { it.exists() }?.toPostReaction())
                }
            awaitClose { listener.remove() }
        }

    //Helpers

    private suspend fun uploadImage(courseId: String, postId: String, uri: Uri): String {
        val ref = storage.reference.child("cursos/$courseId/posts/$postId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private suspend fun deleteImageIfExists(courseId: String, postId: String) {
        try {
            storage.reference.child("cursos/$courseId/posts/$postId.jpg").delete().await()
        } catch (_: Exception) {
            // No image in post
        }
    }

    private fun postsCollection(courseId: String) =
        firestore.collection(COURSES_COLLECTION).document(courseId).collection(POSTS_SUBCOLLECTION)

    private fun reactionsCollection(courseId: String, postId: String) =
        postsCollection(courseId).document(postId).collection(REACTIONS_SUBCOLLECTION)

    private fun DocumentSnapshot.toPost(): Post {
        return Post(
            id = id,
            text = getString(TEXT_FIELD).orEmpty(),
            imageUrl = getString(IMAGE_URL_FIELD),
            createdAt = getTimestamp(CREATED_AT_FIELD)?.toDate()?.time ?: 0L,
            editedAt = getTimestamp(EDITED_AT_FIELD)?.toDate()?.time
        )
    }

    private fun DocumentSnapshot.toPostReaction(): PostReaction {
        return PostReaction(
            studentUid = id,
            studentName = getString(STUDENT_NAME_FIELD).orEmpty().ifBlank { id },
            type = getString(REACTION_TYPE_FIELD).orEmpty(),
            reactedAt = getTimestamp(REACTED_AT_FIELD)?.toDate()?.time ?: 0L
        )
    }
}