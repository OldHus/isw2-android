package com.svape.unal.ingesoftii.domain.model

sealed class AttendanceError : Exception() {
    data object EmptyCode : AttendanceError()
    data object InvalidDuration : AttendanceError()
    data object NoActiveSession : AttendanceError()
    data object InvalidCode : AttendanceError()
    data object SessionExpired : AttendanceError()
    data object AlreadyRegistered : AttendanceError()
    data class Unknown(override val message: String) : AttendanceError()
}