package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.PostError
import com.svape.unal.ingesoftii.domain.model.ReactionType
import com.svape.unal.ingesoftii.domain.repository.PostRepository

class SetMyReactionUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(courseId: String, postId: String, type: String): Result<Unit> {
        if (type !in ReactionType.ALL) return Result.failure(PostError.InvalidReactionType)
        return repository.setMyReaction(courseId, postId, type)
    }
}