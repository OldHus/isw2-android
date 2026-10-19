package com.svape.unal.ingesoftii.domain.model

data class QuizAnswer(
    val studentUid: String,
    val studentName: String,
    val selectedOptionIndex: Int,
    val responseTimeMillis: Long,
    val score: Int,
    val answeredAt: Long,
    val isCorrect: Boolean? = null
)
