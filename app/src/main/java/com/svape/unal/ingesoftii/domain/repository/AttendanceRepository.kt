package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.AttendanceRecord
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {

    // Teacher

    suspend fun startSession(courseId: String, code: String, durationMinutes: Int): Result<AttendanceSession>

    suspend fun closeSession(courseId: String, sessionId: String): Result<Unit>

    fun observeSessionRegistrations(courseId: String, sessionId: String): Flow<List<AttendanceRecord>>

    // Shared

    suspend fun getActiveSession(courseId: String): Result<AttendanceSession?>

    fun observeActiveSession(courseId: String, includeCode: Boolean = false): Flow<AttendanceSession?>

    // Student

    suspend fun submitAttendance(courseId: String, sessionId: String, code: String): Result<Unit>

    suspend fun hasSubmittedAttendance(courseId: String, sessionId: String): Result<Boolean>
}