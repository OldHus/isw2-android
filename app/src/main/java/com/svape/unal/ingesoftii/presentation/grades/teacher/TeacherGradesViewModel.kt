package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.AddGradeItemUseCase
import com.svape.unal.ingesoftii.domain.usecase.CountStudentsGradedUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteGradeItemUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetGradeItemsUseCase
import com.svape.unal.ingesoftii.domain.usecase.RenameGradeItemUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherGradesViewModel(
    private val getGradeItemsUseCase: GetGradeItemsUseCase,
    private val getCourseStudentsUseCase: GetCourseStudentsUseCase,
    private val addGradeItemUseCase: AddGradeItemUseCase,
    private val renameGradeItemUseCase: RenameGradeItemUseCase,
    private val countStudentsGradedUseCase: CountStudentsGradedUseCase,
    private val deleteGradeItemUseCase: DeleteGradeItemUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherGradesUiState>(TeacherGradesUiState.Loading)
    val uiState: StateFlow<TeacherGradesUiState> = _uiState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var courseId: String = ""

    fun load(courseId: String) {
        this.courseId = courseId
        _uiState.value = TeacherGradesUiState.Loading
        viewModelScope.launch {
            getGradeItemsUseCase(courseId)
                .onSuccess { items ->
                    getCourseStudentsUseCase(courseId)
                        .onSuccess { students ->
                            _uiState.value = TeacherGradesUiState.Success(items, students)
                        }
                        .onFailure {
                            _uiState.value =
                                TeacherGradesUiState.Error("No se pudo cargar la lista de estudiantes")
                        }
                }
                .onFailure {
                    _uiState.value =
                        TeacherGradesUiState.Error("No se pudieron cargar los ítems de calificación")
                }
        }
    }

    fun addItem(name: String, weight: Double, type: String) {
        viewModelScope.launch {
            addGradeItemUseCase(courseId, name, weight, type)
                .onSuccess {
                    analyticsReporter.logEvent(
                        name = "grade_item_added",
                        params = mapOf(
                            "course_id" to courseId,
                            "item_name" to name,
                            "weight" to weight,
                            "type" to type
                        )
                    )
                    _message.value = "Ítem \"$name\" agregado"
                    load(courseId)
                }
                .onFailure {
                    _message.value = "No se pudo agregar el ítem"
                }
        }
    }

    fun renameItem(itemId: String, newName: String) {
        viewModelScope.launch {
            renameGradeItemUseCase(courseId, itemId, newName)
                .onSuccess {
                    analyticsReporter.logEvent(
                        name = "grade_item_renamed",
                        params = mapOf("course_id" to courseId, "item_id" to itemId)
                    )
                    _message.value = "Ítem renombrado"
                    load(courseId)
                }
                .onFailure {
                    _message.value = "No se pudo renombrar el ítem"
                }
        }
    }

    suspend fun countStudentsGraded(itemId: String): Int =
        countStudentsGradedUseCase(courseId, itemId).getOrDefault(0)

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            deleteGradeItemUseCase(courseId, itemId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        name = "grade_item_deleted",
                        params = mapOf("course_id" to courseId, "item_id" to itemId)
                    )
                    _message.value = "Ítem eliminado"
                    load(courseId)
                }
                .onFailure {
                    _message.value = "No se pudo eliminar el ítem"
                }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}