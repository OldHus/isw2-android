package com.svape.unal.ingesoftii.domain.model

data class QuizSession(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int?,
    val durationSeconds: Int,
    val isActive: Boolean,
    val launchedAt: Long,
    val expiresAt: Long
)
