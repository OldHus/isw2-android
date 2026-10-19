package com.svape.unal.ingesoftii.domain.repository

import android.net.Uri
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.PostReaction
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    // Teacher

    suspend fun createPost(courseId: String, text: String, imageUri: Uri?): Result<Post>

    suspend fun updatePost(
        courseId: String,
        postId: String,
        text: String,
        newImageUri: Uri?,
        removeImage: Boolean
    ): Result<Unit>

    suspend fun deletePost(courseId: String, postId: String): Result<Unit>

    // Shared

    fun observePosts(courseId: String): Flow<List<Post>>

    fun observePostReactions(courseId: String, postId: String): Flow<List<PostReaction>>

    // Student

    suspend fun setMyReaction(courseId: String, postId: String, type: String): Result<Unit>

    suspend fun removeMyReaction(courseId: String, postId: String): Result<Unit>

    fun observeMyReaction(courseId: String, postId: String): Flow<PostReaction?>
}