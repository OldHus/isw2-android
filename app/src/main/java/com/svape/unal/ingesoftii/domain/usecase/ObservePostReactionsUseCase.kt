package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow

class ObservePostReactionsUseCase(private val repository: PostRepository) {
    operator fun invoke(courseId: String, postId: String): Flow<List<PostReaction>> =
        repository.observePostReactions(courseId, postId)
}