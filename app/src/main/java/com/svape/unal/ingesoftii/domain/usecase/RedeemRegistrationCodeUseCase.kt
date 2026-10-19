package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.RegistrationResult
import com.svape.unal.ingesoftii.domain.repository.CourseRepository

class RedeemRegistrationCodeUseCase(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(code: String): Result<RegistrationResult> {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return Result.failure(IllegalArgumentException("Code cannot be empty"))
        }
        return courseRepository.redeemRegistrationCode(trimmedCode)
    }
}