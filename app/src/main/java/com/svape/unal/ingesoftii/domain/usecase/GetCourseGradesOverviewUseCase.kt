package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.StudentGrades
import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class GetCourseGradesOverviewUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(courseId: String): Result<Map<String, StudentGrades>> {
        return gradeRepository.getAllStudentsGrades(courseId)
    }
}