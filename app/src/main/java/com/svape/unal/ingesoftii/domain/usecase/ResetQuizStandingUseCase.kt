package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.QuizStandingRepository

class ResetQuizStandingUseCase(
    private val quizStandingRepository: QuizStandingRepository
) {
    suspend operator fun invoke(courseId: String): Result<Unit> =
        quizStandingRepository.resetCourseStanding(courseId)
}
