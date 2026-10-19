package com.svape.unal.ingesoftii.presentation.grades.teacher

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem

sealed class TeacherGradesUiState {
    data object Loading : TeacherGradesUiState()
    data class Success(val items: List<GradeItem>, val students: List<CourseStudent>) :
        TeacherGradesUiState()

    data class Error(val message: String) : TeacherGradesUiState()
}