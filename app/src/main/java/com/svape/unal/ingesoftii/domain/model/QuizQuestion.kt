package com.svape.unal.ingesoftii.domain.model

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val createdAt: Long
)