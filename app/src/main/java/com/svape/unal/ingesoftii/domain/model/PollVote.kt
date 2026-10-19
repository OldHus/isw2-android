package com.svape.unal.ingesoftii.domain.model

data class PollVote(
    val studentUid: String,
    val studentName: String,
    val optionIndex: Int,
    val votedAt: Long
)