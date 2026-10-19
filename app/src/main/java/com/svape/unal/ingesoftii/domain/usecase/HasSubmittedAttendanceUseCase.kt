package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository

class HasSubmittedAttendanceUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(courseId: String, sessionId: String): Result<Boolean> {
        return attendanceRepository.hasSubmittedAttendance(courseId, sessionId)
    }
}