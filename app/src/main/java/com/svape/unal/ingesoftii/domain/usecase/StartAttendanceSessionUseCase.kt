package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AttendanceError
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository

private const val MAX_DURATION_MINUTES = 60

class StartAttendanceSessionUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(
        courseId: String,
        code: String,
        durationMinutes: Int
    ): Result<AttendanceSession> {
        val trimmedCode = code.trim()
        if (trimmedCode.isEmpty()) {
            return Result.failure(AttendanceError.EmptyCode)
        }
        if (durationMinutes !in 1..MAX_DURATION_MINUTES) {
            return Result.failure(AttendanceError.InvalidDuration)
        }
        return attendanceRepository.startSession(courseId, trimmedCode, durationMinutes)
    }
}