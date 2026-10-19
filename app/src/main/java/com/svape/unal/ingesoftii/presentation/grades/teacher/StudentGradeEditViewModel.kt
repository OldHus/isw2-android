package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.DeleteStudentGradeUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetGradeItemsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetStudentGradesUseCase
import com.svape.unal.ingesoftii.domain.usecase.SetStudentGradeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentGradeEditViewModel(
    private val getGradeItemsUseCase: GetGradeItemsUseCase,
    private val getStudentGradesUseCase: GetStudentGradesUseCase,
    private val setStudentGradeUseCase: SetStudentGradeUseCase,
    private val deleteStudentGradeUseCase: DeleteStudentGradeUseCase,
    private val getCourseStudentsUseCase: GetCourseStudentsUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<StudentGradeEditUiState>(StudentGradeEditUiState.Loading)
    val uiState: StateFlow<StudentGradeEditUiState> = _uiState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var courseId: String = ""
    private var studentUid: String = ""

    private var student: CourseStudent? = null

    fun load(courseId: String, studentUid: String) {
        if (this.studentUid != studentUid) student = null
        this.courseId = courseId
        this.studentUid = studentUid
        _uiState.value = StudentGradeEditUiState.Loading
        viewModelScope.launch {
            if (student == null) {
                student = getCourseStudentsUseCase(courseId)
                    .getOrNull()
                    ?.firstOrNull { it.uid == studentUid }
            }

            getGradeItemsUseCase(courseId)
                .onSuccess { items ->
                    getStudentGradesUseCase(courseId, studentUid)
                        .onSuccess { studentGrades ->
                            _uiState.value = StudentGradeEditUiState.Success(
                                items = items,
                                grades = studentGrades.grades,
                                student = student,
                            )
                        }
                        .onFailure {
                            _uiState.value =
                                StudentGradeEditUiState.Error("No se pudieron cargar las calificaciones")
                        }
                }
                .onFailure {
                    _uiState.value =
                        StudentGradeEditUiState.Error("No se pudieron cargar los ítems")
                }
        }
    }

    fun saveGrade(itemId: String, grade: Double) {
        viewModelScope.launch {
            setStudentGradeUseCase(courseId, studentUid, itemId, grade)
                .onSuccess {
                    analyticsReporter.logEvent(
                        name = "grade_saved",
                        params = mapOf(
                            "course_id" to courseId,
                            "item_id" to itemId,
                            "grade" to grade
                        )
                    )
                    _message.value = "Nota guardada"
                    load(courseId, studentUid)
                }
                .onFailure {
                    _message.value = "No se pudo guardar la nota"
                }
        }
    }

    fun deleteGrade(itemId: String) {
        viewModelScope.launch {
            deleteStudentGradeUseCase(courseId, studentUid, itemId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        name = "grade_deleted",
                        params = mapOf("course_id" to courseId, "item_id" to itemId)
                    )
                    _message.value = "Nota eliminada"
                    load(courseId, studentUid)
                }
                .onFailure {
                    _message.value = "No se pudo eliminar la nota"
                }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}