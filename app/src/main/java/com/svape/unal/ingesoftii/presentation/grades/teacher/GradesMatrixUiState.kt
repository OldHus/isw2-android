package com.svape.unal.ingesoftii.presentation.grades.teacher

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem

sealed class GradesMatrixUiState {
    data object Loading : GradesMatrixUiState()
    data class Success(
        val items: List<GradeItem>,
        val students: List<CourseStudent>,
        val grades: Map<String, Map<String, Double>>
    ) : GradesMatrixUiState()

    data class Error(val message: String) : GradesMatrixUiState()
}