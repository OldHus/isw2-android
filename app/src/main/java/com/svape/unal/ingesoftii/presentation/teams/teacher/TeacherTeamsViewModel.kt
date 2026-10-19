package com.svape.unal.ingesoftii.presentation.teams.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.CreateTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteTeamUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseTeamsUseCase
import com.svape.unal.ingesoftii.domain.usecase.RenameTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherTeamsViewModel(
    private val getCourseTeamsUseCase: GetCourseTeamsUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val renameTeamUseCase: RenameTeamUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherTeamsUiState>(TeacherTeamsUiState.Loading)
    val uiState: StateFlow<TeacherTeamsUiState> = _uiState.asStateFlow()

    private var courseId: String? = null

    fun loadTeams(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            _uiState.value = TeacherTeamsUiState.Loading
            getCourseTeamsUseCase(courseId)
                .onSuccess { teams -> _uiState.value = TeacherTeamsUiState.Success(teams) }
                .onFailure { error ->
                    _uiState.value =
                        TeacherTeamsUiState.Error(
                            error.message ?: "No se pudieron cargar los equipos"
                        )
                }
        }
    }

    fun renameTeam(teamId: String, newName: String) {
        val currentCourseId = courseId ?: return
        viewModelScope.launch {
            renameTeamUseCase(currentCourseId, teamId, newName)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "team_renamed",
                        mapOf("course_id" to currentCourseId, "team_id" to teamId)
                    )
                    loadTeams(currentCourseId)
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherTeamsUiState.Error(error.message ?: "No se pudo renombrar el equipo")
                }
        }
    }

    fun deleteTeam(teamId: String) {
        val currentCourseId = courseId ?: return
        viewModelScope.launch {
            deleteTeamUseCase(currentCourseId, teamId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "team_deleted",
                        mapOf("course_id" to currentCourseId, "team_id" to teamId)
                    )
                    loadTeams(currentCourseId)
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherTeamsUiState.Error(error.message ?: "No se pudo eliminar el equipo")
                }
        }
    }

    fun createTeam(name: String) {
        val currentCourseId = courseId ?: return
        viewModelScope.launch {
            createTeamUseCase(currentCourseId, name)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "team_created",
                        mapOf("course_id" to currentCourseId)
                    )
                    loadTeams(currentCourseId)
                }
                .onFailure { error ->
                    analyticsReporter.logEvent(
                        "team_create_error",
                        mapOf("course_id" to currentCourseId)
                    )
                    _uiState.value =
                        TeacherTeamsUiState.Error(error.message ?: "No se pudo crear el equipo")
                }
        }
    }
}