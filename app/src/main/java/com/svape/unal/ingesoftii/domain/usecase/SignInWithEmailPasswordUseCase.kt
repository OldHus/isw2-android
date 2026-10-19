package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AuthResult
import com.svape.unal.ingesoftii.domain.repository.AuthRepository

class SignInWithEmailPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResult> {
        return authRepository.signInWithEmailAndPassword(email.trim(), password)
    }
}