package com.svape.unal.ingesoftii.domain.model

sealed class AuthResult {
    data class ExistingUser(val role: String, val courseId: String) : AuthResult()
    data class NewUser(val uid: String) : AuthResult()
}