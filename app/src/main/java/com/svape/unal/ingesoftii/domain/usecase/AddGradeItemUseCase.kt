package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.GradeError
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class AddGradeItemUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(
        courseId: String,
        name: String,
        weight: Double,
        type: String
    ): Result<GradeItem> {
        if (name.isBlank()) {
            return Result.failure(GradeError.Unknown("El nombre del ítem no puede estar vacío"))
        }

        return gradeRepository.addGradeItem(courseId, name.trim(), weight, type)
    }
}