package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.GradeError
import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class SetStudentGradeUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(
        courseId: String,
        studentUid: String,
        itemId: String,
        grade: Double
    ): Result<Unit> {
        if (grade < MIN_GRADE || grade > MAX_GRADE) {
            return Result.failure(GradeError.InvalidGrade("La nota debe estar entre $MIN_GRADE y $MAX_GRADE"))
        }
        return gradeRepository.setStudentGrade(courseId, studentUid, itemId, grade)
    }

    companion object {
        private const val MIN_GRADE = 0.0
        private const val MAX_GRADE = 5.0
    }
}