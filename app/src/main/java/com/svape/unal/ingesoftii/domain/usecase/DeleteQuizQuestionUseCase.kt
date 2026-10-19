package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.QuizRepository

class DeleteQuizQuestionUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(courseId: String, questionId: String): Result<Unit> =
        repository.deleteQuestion(courseId, questionId)
}