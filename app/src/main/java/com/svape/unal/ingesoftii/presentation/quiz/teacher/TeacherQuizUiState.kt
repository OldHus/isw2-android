package com.svape.unal.ingesoftii.presentation.quiz.teacher

import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.presentation.quiz.QuizLeaderboardEntry

sealed class TeacherQuizUiState {
    data object Loading : TeacherQuizUiState()
    data class Error(val message: String) : TeacherQuizUiState()
    data class Content(
        val bank: List<QuizQuestion>,
        val activeSession: QuizSession?,
        val leaderboard: List<QuizLeaderboardEntry>,
        val standing: List<QuizStandingEntry> = emptyList()
    ) : TeacherQuizUiState()
}