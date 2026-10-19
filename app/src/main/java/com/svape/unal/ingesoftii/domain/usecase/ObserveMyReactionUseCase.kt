package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow

class ObserveMyReactionUseCase(private val repository: PostRepository) {
    operator fun invoke(courseId: String, postId: String): Flow<PostReaction?> =
        repository.observeMyReaction(courseId, postId)
}