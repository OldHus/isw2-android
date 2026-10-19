package com.svape.unal.ingesoftii.domain.model

data class Team(
    val id: String,
    val name: String,
    val memberUids: List<String>,
    val createdAt: Long
)