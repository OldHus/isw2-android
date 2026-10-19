package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.repository.CourseRepository

class GetCourseStudentsUseCase(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(courseId: String): Result<List<CourseStudent>> {
        return courseRepository.getCourseStudents(courseId)
    }
}