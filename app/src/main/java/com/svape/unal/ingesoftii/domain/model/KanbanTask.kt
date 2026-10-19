package com.svape.unal.ingesoftii.domain.model

data class KanbanTask(
    val id: String,
    val teamId: String,
    val title: String,
    val description: String,
    val column: TaskColumn,
    val createdByUid: String,
    val order: Int,
    val createdAt: Long,
    val updatedAt: Long
)