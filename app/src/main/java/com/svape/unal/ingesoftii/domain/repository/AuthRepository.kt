package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.AuthResult

interface AuthRepository {
    suspend fun signInWithGoogle(): Result<AuthResult>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult>
    suspend fun getCurrentSession(): AuthResult?
    suspend fun signOut()
}