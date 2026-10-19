package com.svape.unal.ingesoftii.domain.model

sealed class RegistrationResult {
    data class Success(val role: String, val courseId: String) : RegistrationResult()
}

sealed class RegistrationError : Exception() {
    data object InvalidCode : RegistrationError()
    data object UserNotAuthenticated : RegistrationError()
    data class Unknown(override val message: String) : RegistrationError()
}