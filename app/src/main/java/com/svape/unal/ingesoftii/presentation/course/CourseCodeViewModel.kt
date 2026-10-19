package com.svape.unal.ingesoftii.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.RegistrationError
import com.svape.unal.ingesoftii.domain.model.RegistrationResult
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.service.CrashReporter
import com.svape.unal.ingesoftii.domain.usecase.RedeemRegistrationCodeUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubscribeToCourseNotificationsUseCase
import com.svape.unal.ingesoftii.presentation.session.CurrentSessionHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseCodeViewModel(
    private val redeemRegistrationCodeUseCase: RedeemRegistrationCodeUseCase,
    private val subscribeToCourseNotificationsUseCase: SubscribeToCourseNotificationsUseCase,
    private val currentSessionHolder: CurrentSessionHolder,
    private val analyticsReporter: AnalyticsReporter,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseCodeUiState>(CourseCodeUiState.Idle)
    val uiState: StateFlow<CourseCodeUiState> = _uiState.asStateFlow()

    fun onSubmitCode(code: String) {
        if (_uiState.value == CourseCodeUiState.Loading) return

        _uiState.value = CourseCodeUiState.Loading
        viewModelScope.launch {
            redeemRegistrationCodeUseCase(code)
                .onSuccess { result ->
                    when (result) {
                        is RegistrationResult.Success -> {
                            analyticsReporter.logEvent(
                                name = "course_code_redeemed",
                                params = mapOf("course_id" to result.courseId)
                            )

                            subscribeToCourseNotificationsUseCase(result.courseId, result.role)
                                .onFailure { crashReporter.recordException(it) }

                            _uiState.value = CourseCodeUiState.Success(result.role, result.courseId)

                            currentSessionHolder.update(result.role, result.courseId)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = when (error) {
                        is RegistrationError.InvalidCode -> {
                            analyticsReporter.logEvent(
                                name = "course_code_rejected",
                                params = mapOf("reason" to "invalid_code")
                            )
                            CourseCodeUiState.Error("Código inválido. Revísalo e intenta de nuevo")
                        }

                        is RegistrationError.UserNotAuthenticated -> CourseCodeUiState.Error("Tu sesión expiró. Inicia sesión de nuevo")
                        is IllegalArgumentException -> CourseCodeUiState.Error("Ingresa un código")
                        else -> CourseCodeUiState.Error("Algo salió mal. Intenta de nuevo")
                    }
                }
        }
    }
}