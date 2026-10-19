package com.svape.unal.ingesoftii.presentation.session

sealed class SessionUiState {
    data object Loading : SessionUiState()
    data object NotAuthenticated : SessionUiState()
    data class NeedsRegistration(val uid: String) : SessionUiState()
    data class Authenticated(val role: String, val courseId: String) : SessionUiState()
}