package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveAttendanceSessionUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    operator fun invoke(courseId: String, includeCode: Boolean = false): Flow<AttendanceSession?> {
        return attendanceRepository.observeActiveSession(courseId, includeCode)
    }
}