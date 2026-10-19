package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.domain.repository.GradeRepository

class GetGradeItemsUseCase(
    private val gradeRepository: GradeRepository
) {
    suspend operator fun invoke(courseId: String): Result<List<GradeItem>> {
        return gradeRepository.getGradeItems(courseId)
    }
}