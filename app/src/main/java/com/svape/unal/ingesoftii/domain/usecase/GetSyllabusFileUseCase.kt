package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.SyllabusRepository
import java.io.File

class GetSyllabusFileUseCase(
    private val syllabusRepository: SyllabusRepository
) {
    suspend operator fun invoke(courseId: String): Result<File> {
        return syllabusRepository.downloadSyllabus(courseId)
    }
}