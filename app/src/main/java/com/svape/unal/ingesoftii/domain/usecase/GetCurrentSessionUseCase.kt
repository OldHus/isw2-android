package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AuthResult
import com.svape.unal.ingesoftii.domain.repository.AuthRepository

class GetCurrentSessionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult? {
        return authRepository.getCurrentSession()
    }
}