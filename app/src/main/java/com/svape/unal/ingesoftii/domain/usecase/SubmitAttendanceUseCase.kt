package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AttendanceError
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository

class SubmitAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(
        courseId: String,
        session: AttendanceSession,
        enteredCode: String
    ): Result<Unit> {
        if (!session.isActive) {
            return Result.failure(AttendanceError.NoActiveSession)
        }
        if (System.currentTimeMillis() > session.expiresAt) {
            return Result.failure(AttendanceError.SessionExpired)
        }
        val code = enteredCode.trim()
        if (code.isEmpty()) {
            return Result.failure(AttendanceError.InvalidCode)
        }
        return attendanceRepository.submitAttendance(courseId, session.id, code)
    }
}