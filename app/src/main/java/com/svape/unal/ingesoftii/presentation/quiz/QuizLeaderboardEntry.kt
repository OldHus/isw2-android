package com.svape.unal.ingesoftii.presentation.quiz

data class QuizLeaderboardEntry(
    val rank: Int,
    val studentName: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val score: Int,
    val responseTimeMillis: Long
)