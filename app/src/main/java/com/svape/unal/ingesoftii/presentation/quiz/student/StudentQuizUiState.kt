package com.svape.unal.ingesoftii.presentation.quiz.student

import com.svape.unal.ingesoftii.domain.model.QuizAnswer
import com.svape.unal.ingesoftii.domain.model.QuizSession

sealed class StudentQuizUiState {
    data object Loading : StudentQuizUiState()
    data object NoActiveQuestion : StudentQuizUiState()
    data class Question(val session: QuizSession, val myAnswer: QuizAnswer?) : StudentQuizUiState()
    data class Error(val message: String) : StudentQuizUiState()
}