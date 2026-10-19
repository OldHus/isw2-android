package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.model.PollError
import com.svape.unal.ingesoftii.domain.repository.PollRepository

class SubmitVoteUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(courseId: String, poll: Poll, optionIndex: Int): Result<Unit> {
        if (!poll.isActive) {
            return Result.failure(PollError.PollClosed)
        }
        if (System.currentTimeMillis() > poll.expiresAt) {
            return Result.failure(PollError.PollClosed)
        }
        if (optionIndex !in poll.options.indices) {
            return Result.failure(PollError.InvalidOption)
        }
        return pollRepository.submitVote(courseId, poll.id, optionIndex)
    }
}