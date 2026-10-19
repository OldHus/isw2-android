package com.svape.unal.ingesoftii.presentation.grades.teacher

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem

sealed class StudentGradeEditUiState {
    data object Loading : StudentGradeEditUiState()
    data class Success(
        val items: List<GradeItem>,
        val grades: Map<String, Double>,
        val student: CourseStudent? = null,
    ) : StudentGradeEditUiState()

    data class Error(val message: String) : StudentGradeEditUiState()
}