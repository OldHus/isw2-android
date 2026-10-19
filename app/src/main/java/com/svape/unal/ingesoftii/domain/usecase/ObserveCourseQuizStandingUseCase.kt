package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.domain.repository.QuizStandingRepository
import kotlinx.coroutines.flow.Flow

class ObserveCourseQuizStandingUseCase(
    private val quizStandingRepository: QuizStandingRepository
) {
    operator fun invoke(courseId: String): Flow<List<QuizStandingEntry>> =
        quizStandingRepository.observeCourseStanding(courseId)
}
