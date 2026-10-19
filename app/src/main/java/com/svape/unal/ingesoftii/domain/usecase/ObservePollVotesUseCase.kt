package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.PollVote
import com.svape.unal.ingesoftii.domain.repository.PollRepository
import kotlinx.coroutines.flow.Flow

class ObservePollVotesUseCase(
    private val pollRepository: PollRepository
) {
    operator fun invoke(courseId: String, pollId: String): Flow<List<PollVote>> {
        return pollRepository.observePollVotes(courseId, pollId)
    }
}