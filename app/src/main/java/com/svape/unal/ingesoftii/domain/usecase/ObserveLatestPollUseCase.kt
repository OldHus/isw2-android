package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.repository.PollRepository
import kotlinx.coroutines.flow.Flow

class ObserveLatestPollUseCase(
    private val pollRepository: PollRepository
) {
    operator fun invoke(courseId: String): Flow<Poll?> {
        return pollRepository.observeLatestPoll(courseId)
    }
}