package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizError
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.repository.QuizRepository

class SubmitQuizAnswerUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(
        courseId: String,
        session: QuizSession,
        selectedOptionIndex: Int
    ): Result<Unit> {
        if (selectedOptionIndex !in session.options.indices) {
            return Result.failure(QuizError.InvalidOption)
        }
        if (!session.isActive) return Result.failure(QuizError.SessionClosed)
        return repository.submitAnswer(courseId, session, selectedOptionIndex)
    }
}