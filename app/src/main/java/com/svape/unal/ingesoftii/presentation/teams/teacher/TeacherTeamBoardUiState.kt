package com.svape.unal.ingesoftii.presentation.teams.teacher

import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.Team

sealed class TeacherTeamBoardUiState {
    data object Loading : TeacherTeamBoardUiState()
    data class Success(
        val team: Team,
        val members: List<CourseStudent>,
        val availableStudents: List<CourseStudent>,
        val tasks: List<KanbanTask>
    ) : TeacherTeamBoardUiState()
    data class Error(val message: String) : TeacherTeamBoardUiState()
}