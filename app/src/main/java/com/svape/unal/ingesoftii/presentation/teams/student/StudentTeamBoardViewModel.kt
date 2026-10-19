package com.svape.unal.ingesoftii.presentation.teams.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.CreateTaskUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetMyTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetTeamTasksUseCase
import com.svape.unal.ingesoftii.domain.usecase.MoveTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentTeamBoardViewModel(
    private val getMyTeamUseCase: GetMyTeamUseCase,
    private val getTeamTasksUseCase: GetTeamTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<StudentTeamBoardUiState>(StudentTeamBoardUiState.Loading)
    val uiState: StateFlow<StudentTeamBoardUiState> = _uiState.asStateFlow()

    private var courseId: String = ""
    private var teamId: String? = null

    fun load(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            _uiState.value = StudentTeamBoardUiState.Loading

            val team = getMyTeamUseCase(courseId).getOrElse {
                _uiState.value =
                    StudentTeamBoardUiState.Error(it.message ?: "No se pudo cargar tu equipo")
                return@launch
            }

            if (team == null) {
                teamId = null
                _uiState.value = StudentTeamBoardUiState.NoTeamAssigned
                return@launch
            }

            teamId = team.id
            val tasks = getTeamTasksUseCase(courseId, team.id).getOrElse {
                _uiState.value =
                    StudentTeamBoardUiState.Error(it.message ?: "No se pudieron cargar las tareas")
                return@launch
            }

            _uiState.value = StudentTeamBoardUiState.Success(team, tasks)
        }
    }

    fun createTask(title: String, description: String) {
        val currentTeamId = teamId ?: return
        viewModelScope.launch {
            createTaskUseCase(courseId, currentTeamId, title, description)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "task_created",
                        mapOf("course_id" to courseId, "team_id" to currentTeamId)
                    )
                    load(courseId)
                }
                .onFailure {
                    _uiState.value =
                        StudentTeamBoardUiState.Error(it.message ?: "No se pudo crear la propuesta")
                }
        }
    }


    fun moveTask(taskId: String, from: TaskColumn, to: TaskColumn) {
        val currentTeamId = teamId ?: return
        viewModelScope.launch {
            moveTaskUseCase(courseId, currentTeamId, taskId, UserRole.STUDENT, from, to)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "task_moved",
                        mapOf(
                            "course_id" to courseId,
                            "team_id" to currentTeamId,
                            "from" to from.firestoreValue,
                            "to" to to.firestoreValue
                        )
                    )
                    load(courseId)
                }
                .onFailure {
                    _uiState.value =
                        StudentTeamBoardUiState.Error(it.message ?: "No se pudo mover la tarea")
                }
        }
    }
}