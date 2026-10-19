package com.svape.unal.ingesoftii.presentation.grades.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.usecase.GetGradeItemsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetMyGradesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentGradesViewModel(
    private val getGradeItemsUseCase: GetGradeItemsUseCase,
    private val getMyGradesUseCase: GetMyGradesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentGradesUiState>(StudentGradesUiState.Loading)
    val uiState: StateFlow<StudentGradesUiState> = _uiState.asStateFlow()

    fun loadGrades(courseId: String) {
        _uiState.value = StudentGradesUiState.Loading
        viewModelScope.launch {
            getGradeItemsUseCase(courseId)
                .onSuccess { items ->
                    getMyGradesUseCase(courseId)
                        .onSuccess { studentGrades ->
                            _uiState.value =
                                StudentGradesUiState.Success(items, studentGrades.grades)
                        }
                        .onFailure {
                            _uiState.value =
                                StudentGradesUiState.Error("No se pudieron cargar tus calificaciones")
                        }
                }
                .onFailure {
                    _uiState.value =
                        StudentGradesUiState.Error("No se pudieron cargar los ítems de calificación")
                }
        }
    }
}