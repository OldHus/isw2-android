package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.StudentGrades
import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class GetStudentGradesUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(courseId: String, studentUid: String): Result<StudentGrades> {
        return gradeRepository.getStudentGrades(courseId, studentUid)
    }
}