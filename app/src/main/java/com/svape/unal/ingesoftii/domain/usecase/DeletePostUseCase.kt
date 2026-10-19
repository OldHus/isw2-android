package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.PostRepository

class DeletePostUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(courseId: String, postId: String): Result<Unit> =
        repository.deletePost(courseId, postId)
}