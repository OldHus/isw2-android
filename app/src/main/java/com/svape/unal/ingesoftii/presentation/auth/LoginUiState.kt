package com.svape.unal.ingesoftii.presentation.auth

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class ExistingUserSuccess(val role: String, val courseId: String) : LoginUiState()
    data class NewUserSuccess(val uid: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}