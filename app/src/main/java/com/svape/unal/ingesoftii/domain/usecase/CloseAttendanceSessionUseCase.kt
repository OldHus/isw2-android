package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository

class CloseAttendanceSessionUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(courseId: String, sessionId: String): Result<Unit> {
        return attendanceRepository.closeSession(courseId, sessionId)
    }
}