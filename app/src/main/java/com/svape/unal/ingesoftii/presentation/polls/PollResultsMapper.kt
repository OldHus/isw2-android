package com.svape.unal.ingesoftii.presentation.polls

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.model.PollVote

fun buildPollOptionResults(poll: Poll, votes: List<PollVote>): List<PollOptionResult> {
    return poll.options.mapIndexed { index, optionText ->
        val voterNames = votes.filter { it.optionIndex == index }.map { it.studentName }
        PollOptionResult(optionText, voterNames)
    }
}