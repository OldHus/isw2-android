package com.svape.unal.ingesoftii.presentation.attendance.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.AttendanceError
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.HasSubmittedAttendanceUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveActiveAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitAttendanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StudentAttendanceViewModel(
    private val observeActiveAttendanceSessionUseCase: ObserveActiveAttendanceSessionUseCase,
    private val hasSubmittedAttendanceUseCase: HasSubmittedAttendanceUseCase,
    private val submitAttendanceUseCase: SubmitAttendanceUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<StudentAttendanceUiState>(StudentAttendanceUiState.Loading)
    val uiState: StateFlow<StudentAttendanceUiState> = _uiState.asStateFlow()

    private var courseId: String = ""

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observeActiveAttendanceSessionUseCase(courseId)
                .catch { e ->
                    _uiState.value = StudentAttendanceUiState.Error(
                        e.message ?: "No se pudo cargar la sesión de asistencia"
                    )
                }
                .collect { session -> onSessionUpdate(session) }
        }
    }

    private suspend fun onSessionUpdate(session: AttendanceSession?) {
        if (session == null) {
            _uiState.value = StudentAttendanceUiState.NoActiveSession
            return
        }
        val alreadySubmitted =
            hasSubmittedAttendanceUseCase(courseId, session.id).getOrDefault(false)
        _uiState.value = if (alreadySubmitted) {
            StudentAttendanceUiState.AlreadySubmitted(session)
        } else {
            StudentAttendanceUiState.PendingInput(session)
        }
    }

    fun submitCode(enteredCode: String) {
        val session = (uiState.value as? StudentAttendanceUiState.PendingInput)?.session ?: return
        viewModelScope.launch {
            submitAttendanceUseCase(courseId, session, enteredCode)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "attendance_submitted",
                        mapOf("course_id" to courseId, "session_id" to session.id)
                    )
                    _uiState.value = StudentAttendanceUiState.AlreadySubmitted(session)
                }
                .onFailure { error ->
                    analyticsReporter.logEvent(
                        "attendance_submit_error",
                        mapOf("course_id" to courseId, "session_id" to session.id)
                    )
                    _uiState.value = StudentAttendanceUiState.PendingInput(
                        session = session,
                        errorMessage = mapErrorMessage(error)
                    )
                }
        }
    }

    private fun mapErrorMessage(error: Throwable): String = when (error) {
        is AttendanceError.InvalidCode -> "El código ingresado no es correcto"
        is AttendanceError.SessionExpired -> "El tiempo para registrar asistencia expiró"
        is AttendanceError.NoActiveSession -> "No hay una sesión de asistencia activa"
        is AttendanceError.AlreadyRegistered -> "Ya habías registrado tu asistencia"
        else -> error.message ?: "No se pudo registrar tu asistencia"
    }
}