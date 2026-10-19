package com.svape.unal.ingesoftii.domain.model

data class AttendanceSession(
    val id: String,
    val code: String,
    val isActive: Boolean,
    val durationMinutes: Int,
    val createdAt: Long,
    val expiresAt: Long
)