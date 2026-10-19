package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class RenameGradeItemUseCase(private val gradeRepository: GradeRepository) {
    suspend operator fun invoke(courseId: String, itemId: String, newName: String): Result<Unit> {
        val name = newName.trim()
        if (name.isEmpty()) return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        return gradeRepository.renameGradeItem(courseId, itemId, name)
    }
}

class CountStudentsGradedUseCase(private val gradeRepository: GradeRepository) {
    suspend operator fun invoke(courseId: String, itemId: String): Result<Int> =
        gradeRepository.countStudentsGraded(courseId, itemId)
}

class DeleteGradeItemUseCase(private val gradeRepository: GradeRepository) {
    suspend operator fun invoke(courseId: String, itemId: String): Result<Unit> =
        gradeRepository.deleteGradeItem(courseId, itemId)
}