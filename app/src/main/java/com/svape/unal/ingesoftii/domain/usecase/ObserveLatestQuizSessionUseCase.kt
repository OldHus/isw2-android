package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

class ObserveLatestQuizSessionUseCase(private val repository: QuizRepository) {
    operator fun invoke(courseId: String, withAnswerKey: Boolean = false): Flow<QuizSession?> =
        repository.observeLatestSession(courseId, withAnswerKey)
}
