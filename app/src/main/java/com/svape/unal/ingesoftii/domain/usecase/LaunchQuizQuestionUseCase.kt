package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizError
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.repository.QuizRepository

private const val MAX_DURATION_SECONDS = 300

class LaunchQuizQuestionUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(
        courseId: String,
        question: QuizQuestion,
        durationSeconds: Int
    ): Result<QuizSession> {
        if (durationSeconds !in 1..MAX_DURATION_SECONDS) {
            return Result.failure(QuizError.InvalidDuration)
        }
        return repository.launchQuestion(courseId, question, durationSeconds)
    }
}