package com.svape.unal.ingesoftii.presentation.polls.student

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.presentation.polls.PollOptionResult

sealed class StudentPollsUiState {
    data object Loading : StudentPollsUiState()
    data object NoPoll : StudentPollsUiState()
    data class Result(
        val poll: Poll,
        val optionResults: List<PollOptionResult>,
        val myOptionIndex: Int?
    ) : StudentPollsUiState()
    data class Error(val message: String) : StudentPollsUiState()
}