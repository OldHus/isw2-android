package com.svape.unal.ingesoftii.presentation.teams.student

import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.Team

sealed class StudentTeamBoardUiState {
    data object Loading : StudentTeamBoardUiState()
    data object NoTeamAssigned : StudentTeamBoardUiState()
    data class Success(val team: Team, val tasks: List<KanbanTask>) : StudentTeamBoardUiState()
    data class Error(val message: String) : StudentTeamBoardUiState()
}