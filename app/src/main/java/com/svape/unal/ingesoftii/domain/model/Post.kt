package com.svape.unal.ingesoftii.domain.model

data class Post(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val createdAt: Long,
    val editedAt: Long?
)