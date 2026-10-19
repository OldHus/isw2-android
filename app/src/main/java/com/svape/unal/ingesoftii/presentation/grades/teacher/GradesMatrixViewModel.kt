package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.usecase.GetCourseGradesOverviewUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetGradeItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GradesMatrixViewModel(
    private val getGradeItemsUseCase: GetGradeItemsUseCase,
    private val getCourseStudentsUseCase: GetCourseStudentsUseCase,
    private val getCourseGradesOverviewUseCase: GetCourseGradesOverviewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GradesMatrixUiState>(GradesMatrixUiState.Loading)
    val uiState: StateFlow<GradesMatrixUiState> = _uiState.asStateFlow()

    fun load(courseId: String) {
        _uiState.value = GradesMatrixUiState.Loading
        viewModelScope.launch {
            getGradeItemsUseCase(courseId)
                .onSuccess { items ->
                    getCourseStudentsUseCase(courseId)
                        .onSuccess { students ->
                            getCourseGradesOverviewUseCase(courseId)
                                .onSuccess { gradesByStudent ->
                                    _uiState.value = GradesMatrixUiState.Success(
                                        items = items,
                                        students = students,
                                        grades = gradesByStudent.mapValues { it.value.grades }
                                    )
                                }
                                .onFailure {
                                    _uiState.value =
                                        GradesMatrixUiState.Error("No se pudieron cargar las calificaciones")
                                }
                        }
                        .onFailure {
                            _uiState.value =
                                GradesMatrixUiState.Error("No se pudo cargar la lista de estudiantes")
                        }
                }
                .onFailure {
                    _uiState.value = GradesMatrixUiState.Error("No se pudieron cargar los ítems")
                }
        }
    }
}