package com.svape.unal.ingesoftii.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.core.logging.SafeLog
import com.svape.unal.ingesoftii.domain.model.AuthError
import com.svape.unal.ingesoftii.domain.model.AuthResult
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import com.svape.unal.ingesoftii.domain.usecase.SignInWithEmailPasswordUseCase
import com.svape.unal.ingesoftii.domain.usecase.SignInWithGoogleUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubscribeToCourseNotificationsUseCase
import com.svape.unal.ingesoftii.presentation.session.CurrentSessionHolder
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithEmailPasswordUseCase: SignInWithEmailPasswordUseCase,
    private val subscribeToCourseNotificationsUseCase: SubscribeToCourseNotificationsUseCase,
    private val currentSessionHolder: CurrentSessionHolder,
    private val crashReporter: CrashReporter,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSignInClicked() {
        if (_uiState.value == LoginUiState.Loading) return

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            signInWithGoogleUseCase()
                .onSuccess { result -> handleAuthSuccess(result, method = "google") }
                .onFailure { error -> handleAuthFailure(error, method = "google") }
        }
    }

    fun onSignInWithEmailClicked(email: String, password: String) {
        if (_uiState.value == LoginUiState.Loading) return

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Ingresa tu correo y contraseña.")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            signInWithEmailPasswordUseCase(email, password)
                .onSuccess { result -> handleAuthSuccess(result, method = "email") }
                .onFailure { error -> handleAuthFailure(error, method = "email") }
        }
    }

    private fun handleAuthFailure(error: Throwable, method: String) {
        val authError = error as? AuthError ?: AuthError.Unknown(SafeLog.describe(error))

        SafeLog.w(TAG, "Sign-in failed ($method): ${authError.code}")

        val params = buildMap {
            put("reason", authError.code)
            put("method", method)
            if (authError is AuthError.Unknown) put("detail", authError.detail.take(100))
        }
        analyticsReporter.logEvent(name = "sign_in_error", params = params)

        _uiState.value = LoginUiState.Error(authError.toUserMessage())
    }

    private fun handleAuthSuccess(result: AuthResult, method: String) {
        _uiState.value = when (result) {
            is AuthResult.ExistingUser -> {
                analyticsReporter.logEvent(
                    name = "sign_in_success",
                    params = mapOf(
                        "user_type" to "existing",
                        "role" to result.role,
                        "method" to method
                    )
                )
                currentSessionHolder.update(result.role, result.courseId)
                subscribeToNotifications(result.courseId, result.role)
                LoginUiState.ExistingUserSuccess(result.role, result.courseId)
            }

            is AuthResult.NewUser -> {
                analyticsReporter.logEvent(
                    name = "sign_in_success",
                    params = mapOf("user_type" to "new", "method" to method)
                )
                LoginUiState.NewUserSuccess(result.uid)
            }
        }
    }

    private fun subscribeToNotifications(courseId: String, role: String) {
        viewModelScope.launch {
            withContext(NonCancellable) {
                subscribeToCourseNotificationsUseCase(courseId, role)
                    .onFailure { crashReporter.recordException(it) }
            }
        }
    }

    private companion object {
        const val TAG = "AuthViewModel"
    }
}