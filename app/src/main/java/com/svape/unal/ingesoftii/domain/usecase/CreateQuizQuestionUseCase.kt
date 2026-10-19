package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizError
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.repository.QuizRepository

class CreateQuizQuestionUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(
        courseId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ): Result<QuizQuestion> {
        if (text.isBlank()) return Result.failure(QuizError.EmptyQuestionText)
        if (options.count { it.isNotBlank() } < 2) return Result.failure(QuizError.NotEnoughOptions)
        if (correctOptionIndex !in options.indices) return Result.failure(QuizError.InvalidCorrectOption)
        return repository.createQuestion(courseId, text, options, correctOptionIndex)
    }
}