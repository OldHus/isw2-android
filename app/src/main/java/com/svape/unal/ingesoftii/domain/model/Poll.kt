package com.svape.unal.ingesoftii.domain.model

data class Poll(
    val id: String,
    val question: String,
    val options: List<String>,
    val isActive: Boolean,
    val createdAt: Long,
    val expiresAt: Long
)