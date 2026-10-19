package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.model.PollError
import com.svape.unal.ingesoftii.domain.repository.PollRepository

private const val MIN_LEAD_MILLIS = 60_000L
private const val MAX_LEAD_MILLIS = 30L * 24 * 60 * 60 * 1000

class StartPollUseCase(
    private val pollRepository: PollRepository
) {
    suspend operator fun invoke(
        courseId: String,
        question: String,
        options: List<String>,
        expiresAtMillis: Long
    ): Result<Poll> {
        val trimmedQuestion = question.trim()
        if (trimmedQuestion.isEmpty()) {
            return Result.failure(PollError.EmptyQuestion)
        }
        val cleanOptions = options.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleanOptions.size < 2) {
            return Result.failure(PollError.NotEnoughOptions)
        }
        val leadTime = expiresAtMillis - System.currentTimeMillis()
        if (leadTime < MIN_LEAD_MILLIS || leadTime > MAX_LEAD_MILLIS) {
            return Result.failure(PollError.InvalidExpiration)
        }
        return pollRepository.startPoll(courseId, trimmedQuestion, cleanOptions, expiresAtMillis)
    }
}