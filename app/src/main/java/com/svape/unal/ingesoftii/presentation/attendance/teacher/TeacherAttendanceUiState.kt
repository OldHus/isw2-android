package com.svape.unal.ingesoftii.presentation.attendance.teacher

import com.svape.unal.ingesoftii.domain.model.AttendanceSession

data class AttendanceEntry(
    val studentName: String,
    val studentEmail: String,
    val registeredAtMillis: Long,
    val photoUrl: String? = null,
)

sealed class TeacherAttendanceUiState {
    data object Loading : TeacherAttendanceUiState()
    data object NoActiveSession : TeacherAttendanceUiState()
    data class Active(
        val session: AttendanceSession,
        val entries: List<AttendanceEntry>,
        val totalStudents: Int = 0,
    ) : TeacherAttendanceUiState()

    data class Error(val message: String) : TeacherAttendanceUiState()
}