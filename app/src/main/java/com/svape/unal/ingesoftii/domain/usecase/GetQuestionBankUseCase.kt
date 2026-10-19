package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.repository.QuizRepository

class GetQuestionBankUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(courseId: String): Result<List<QuizQuestion>> =
        repository.getQuestionBank(courseId)
}