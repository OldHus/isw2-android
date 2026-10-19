package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.RegistrationResult

interface CourseRepository {
    suspend fun redeemRegistrationCode(code: String): Result<RegistrationResult>
    suspend fun getCourseStudents(courseId: String): Result<List<CourseStudent>>
}