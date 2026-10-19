package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.model.PollVote
import kotlinx.coroutines.flow.Flow

interface PollRepository {

    // Teacher

    suspend fun startPoll(
        courseId: String,
        question: String,
        options: List<String>,
        expiresAtMillis: Long
    ): Result<Poll>

    suspend fun closePoll(courseId: String, pollId: String): Result<Unit>

    // Shared

    fun observeLatestPoll(courseId: String): Flow<Poll?>

    fun observePollVotes(courseId: String, pollId: String): Flow<List<PollVote>>

    // Student

    suspend fun submitVote(courseId: String, pollId: String, optionIndex: Int): Result<Unit>

    fun observeMyVote(courseId: String, pollId: String): Flow<PollVote?>
}