package com.svape.unal.ingesoftii.domain.model

data class QuizStandingEntry(
    val studentUid: String,
    val studentName: String,
    val totalScore: Int,
    val isCurrentUser: Boolean
)
