package com.svape.unal.ingesoftii.domain.usecase

import android.net.Uri
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.PostError
import com.svape.unal.ingesoftii.domain.repository.PostRepository

class CreatePostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(courseId: String, text: String, imageUri: Uri?): Result<Post> {
        if (text.isBlank() && imageUri == null) return Result.failure(PostError.EmptyContent)
        return repository.createPost(courseId, text, imageUri)
    }
}