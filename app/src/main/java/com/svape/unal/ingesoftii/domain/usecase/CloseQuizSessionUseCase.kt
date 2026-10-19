package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.QuizRepository

class CloseQuizSessionUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(courseId: String, sessionId: String): Result<Unit> =
        repository.closeSession(courseId, sessionId)
}