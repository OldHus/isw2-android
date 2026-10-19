package com.svape.unal.ingesoftii.presentation.posts

data class ReactionSummary(
    val type: String,
    val emoji: String,
    val count: Int,
    val reactorNames: List<String>
)