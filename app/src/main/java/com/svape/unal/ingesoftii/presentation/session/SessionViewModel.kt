package com.svape.unal.ingesoftii.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.AuthResult
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import com.svape.unal.ingesoftii.domain.usecase.GetCurrentSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubscribeToCourseNotificationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionViewModel(
    private val getCurrentSessionUseCase: GetCurrentSessionUseCase,
    private val subscribeToCourseNotificationsUseCase: SubscribeToCourseNotificationsUseCase,
    private val currentSessionHolder: CurrentSessionHolder,
    private val crashReporter: CrashReporter,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _uiState.value = when (val result = getCurrentSessionUseCase()) {
                null -> SessionUiState.NotAuthenticated
                is AuthResult.ExistingUser -> {
                    crashReporter.setUserRole(result.role)
                    crashReporter.setCourseId(result.courseId)
                    analyticsReporter.setUserRole(result.role)
                    analyticsReporter.setCourseId(result.courseId)
                    currentSessionHolder.update(result.role, result.courseId)

                    subscribeToCourseNotificationsUseCase(result.courseId, result.role)
                        .onFailure { crashReporter.recordException(it) }

                    SessionUiState.Authenticated(result.role, result.courseId)
                }

                is AuthResult.NewUser -> SessionUiState.NeedsRegistration(result.uid)
            }
        }
    }
}