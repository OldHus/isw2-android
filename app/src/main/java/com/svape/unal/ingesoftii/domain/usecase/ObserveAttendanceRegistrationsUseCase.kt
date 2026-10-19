package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.AttendanceRecord
import com.svape.unal.ingesoftii.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow

class ObserveAttendanceRegistrationsUseCase(
    private val attendanceRepository: AttendanceRepository
) {
    operator fun invoke(courseId: String, sessionId: String): Flow<List<AttendanceRecord>> {
        return attendanceRepository.observeSessionRegistrations(courseId, sessionId)
    }
}