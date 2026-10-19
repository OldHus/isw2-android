package com.svape.unal.ingesoftii.presentation.teams.teacher

import com.svape.unal.ingesoftii.domain.model.Team

sealed class TeacherTeamsUiState {
    data object Loading : TeacherTeamsUiState()
    data class Success(val teams: List<Team>) : TeacherTeamsUiState()
    data class Error(val message: String) : TeacherTeamsUiState()
}