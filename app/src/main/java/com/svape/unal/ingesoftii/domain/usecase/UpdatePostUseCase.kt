package com.svape.unal.ingesoftii.domain.usecase

import android.net.Uri
import com.svape.unal.ingesoftii.domain.repository.PostRepository

class UpdatePostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(
        courseId: String,
        postId: String,
        text: String,
        newImageUri: Uri?,
        removeImage: Boolean
    ): Result<Unit> = repository.updatePost(courseId, postId, text, newImageUri, removeImage)
}