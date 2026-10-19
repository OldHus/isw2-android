package com.svape.unal.ingesoftii.presentation.attendance.student

import com.svape.unal.ingesoftii.domain.model.AttendanceSession

sealed class StudentAttendanceUiState {
    data object Loading : StudentAttendanceUiState()
    data object NoActiveSession : StudentAttendanceUiState()
    data class PendingInput(val session: AttendanceSession, val errorMessage: String? = null) :
        StudentAttendanceUiState()

    data class AlreadySubmitted(val session: AttendanceSession) : StudentAttendanceUiState()
    data class Error(val message: String) : StudentAttendanceUiState()
}