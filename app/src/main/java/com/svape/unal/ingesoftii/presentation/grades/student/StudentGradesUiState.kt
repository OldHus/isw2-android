package com.svape.unal.ingesoftii.presentation.grades.student

import com.svape.unal.ingesoftii.domain.model.GradeItem

sealed class StudentGradesUiState {
    data object Loading : StudentGradesUiState()
    data class Success(val items: List<GradeItem>, val grades: Map<String, Double>) :
        StudentGradesUiState()

    data class Error(val message: String) : StudentGradesUiState()
}