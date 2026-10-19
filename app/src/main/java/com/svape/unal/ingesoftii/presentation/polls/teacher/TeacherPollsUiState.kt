package com.svape.unal.ingesoftii.presentation.polls.teacher

import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.presentation.polls.PollOptionResult

sealed class TeacherPollsUiState {
    data object Loading : TeacherPollsUiState()
    data object NoPoll : TeacherPollsUiState()
    data class Result(
        val poll: Poll,
        val optionResults: List<PollOptionResult>
    ) : TeacherPollsUiState()
    data class Error(val message: String) : TeacherPollsUiState()
}