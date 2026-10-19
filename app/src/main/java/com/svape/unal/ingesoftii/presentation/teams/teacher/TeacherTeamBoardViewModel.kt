package com.svape.unal.ingesoftii.presentation.teams.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.AddStudentToTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreateTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetTeamByIdUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetTeamTasksUseCase
import com.svape.unal.ingesoftii.domain.usecase.MoveTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.RemoveStudentFromTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherTeamBoardViewModel(
    private val getTeamByIdUseCase: GetTeamByIdUseCase,
    private val getTeamTasksUseCase: GetTeamTasksUseCase,
    private val getCourseStudentsUseCase: GetCourseStudentsUseCase,
    private val addStudentToTeamUseCase: AddStudentToTeamUseCase,
    private val removeStudentFromTeamUseCase: RemoveStudentFromTeamUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TeacherTeamBoardUiState>(TeacherTeamBoardUiState.Loading)
    val uiState: StateFlow<TeacherTeamBoardUiState> = _uiState.asStateFlow()

    private var courseId: String = ""
    private var teamId: String = ""

    fun load(courseId: String, teamId: String) {
        this.courseId = courseId
        this.teamId = teamId
        viewModelScope.launch {
            _uiState.value = TeacherTeamBoardUiState.Loading

            val teamResult = getTeamByIdUseCase(courseId, teamId)
            val team = teamResult.getOrNull()
            if (team == null) {
                _uiState.value = TeacherTeamBoardUiState.Error(
                    teamResult.exceptionOrNull()?.message ?: "No se pudo cargar el equipo"
                )
                return@launch
            }

            val studentsResult = getCourseStudentsUseCase(courseId)
            val allStudents = studentsResult.getOrElse {
                _uiState.value = TeacherTeamBoardUiState.Error(
                    it.message ?: "No se pudieron cargar los estudiantes"
                )
                return@launch
            }

            val tasksResult = getTeamTasksUseCase(courseId, teamId)
            val tasks = tasksResult.getOrElse {
                _uiState.value = TeacherTeamBoardUiState.Error(
                    it.message ?: "No se pudieron cargar las tareas"
                )
                return@launch
            }

            val members = allStudents.filter { it.uid in team.memberUids }
            val available = allStudents.filterNot { it.uid in team.memberUids }

            _uiState.value = TeacherTeamBoardUiState.Success(
                team = team,
                members = members,
                availableStudents = available,
                tasks = tasks
            )
        }
    }

    fun addStudent(student: CourseStudent) {
        viewModelScope.launch {
            addStudentToTeamUseCase(courseId, teamId, student.uid)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "student_added_to_team",
                        mapOf("course_id" to courseId, "team_id" to teamId)
                    )
                    load(courseId, teamId)
                }
                .onFailure {
                    _uiState.value =
                        TeacherTeamBoardUiState.Error(
                            it.message ?: "No se pudo agregar el estudiante"
                        )
                }
        }
    }

    fun removeStudent(student: CourseStudent) {
        viewModelScope.launch {
            removeStudentFromTeamUseCase(courseId, teamId, student.uid)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "student_removed_from_team",
                        mapOf("course_id" to courseId, "team_id" to teamId)
                    )
                    load(courseId, teamId)
                }
                .onFailure {
                    _uiState.value =
                        TeacherTeamBoardUiState.Error(
                            it.message ?: "No se pudo quitar el estudiante"
                        )
                }
        }
    }

    fun createTask(title: String, description: String) {
        viewModelScope.launch {
            createTaskUseCase(courseId, teamId, title, description)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "task_created",
                        mapOf("course_id" to courseId, "team_id" to teamId)
                    )
                    load(courseId, teamId)
                }
                .onFailure {
                    _uiState.value =
                        TeacherTeamBoardUiState.Error(it.message ?: "No se pudo crear la tarea")
                }
        }
    }

    fun moveTask(taskId: String, from: TaskColumn, to: TaskColumn) {
        viewModelScope.launch {
            moveTaskUseCase(courseId, teamId, taskId, UserRole.TEACHER, from, to)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "task_moved",
                        mapOf(
                            "course_id" to courseId,
                            "team_id" to teamId,
                            "from" to from.firestoreValue,
                            "to" to to.firestoreValue
                        )
                    )
                    load(courseId, teamId)
                }
                .onFailure {
                    _uiState.value =
                        TeacherTeamBoardUiState.Error(it.message ?: "No se pudo mover la tarea")
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(courseId, teamId, taskId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "task_rejected",
                        mapOf("course_id" to courseId, "team_id" to teamId)
                    )
                    load(courseId, teamId)
                }
                .onFailure {
                    _uiState.value =
                        TeacherTeamBoardUiState.Error(it.message ?: "No se pudo eliminar la tarea")
                }
        }
    }
}