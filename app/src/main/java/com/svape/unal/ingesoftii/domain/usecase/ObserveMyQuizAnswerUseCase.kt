package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizAnswer
import com.svape.unal.ingesoftii.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

class ObserveMyQuizAnswerUseCase(private val repository: QuizRepository) {
    operator fun invoke(courseId: String, sessionId: String): Flow<QuizAnswer?> =
        repository.observeMyAnswer(courseId, sessionId)
}