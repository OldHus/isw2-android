package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.PollRepository

class ClosePollUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(courseId: String, pollId: String): Result<Unit> {
        return pollRepository.closePoll(courseId, pollId)
    }
}