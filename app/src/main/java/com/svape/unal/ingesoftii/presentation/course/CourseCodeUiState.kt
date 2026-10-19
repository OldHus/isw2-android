package com.svape.unal.ingesoftii.presentation.course

sealed class CourseCodeUiState {
    data object Idle : CourseCodeUiState()
    data object Loading : CourseCodeUiState()
    data class Success(val role: String, val courseId: String) : CourseCodeUiState()
    data class Error(val message: String) : CourseCodeUiState()
}