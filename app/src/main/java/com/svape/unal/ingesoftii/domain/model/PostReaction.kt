package com.svape.unal.ingesoftii.domain.model

data class PostReaction(
    val studentUid: String,
    val studentName: String,
    val type: String,
    val reactedAt: Long
)