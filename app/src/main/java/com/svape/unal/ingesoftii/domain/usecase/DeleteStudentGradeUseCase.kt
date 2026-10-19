package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class DeleteStudentGradeUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(
        courseId: String,
        studentUid: String,
        itemId: String
    ): Result<Unit> {
        return gradeRepository.deleteStudentGrade(courseId, studentUid, itemId)
    }
}