package com.svape.unal.ingesoftii.presentation.polls.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestPollUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyVoteUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePollVotesUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitVoteUseCase
import com.svape.unal.ingesoftii.presentation.polls.buildPollOptionResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class StudentPollsViewModel(
    private val observeLatestPollUseCase: ObserveLatestPollUseCase,
    private val observePollVotesUseCase: ObservePollVotesUseCase,
    private val observeMyVoteUseCase: ObserveMyVoteUseCase,
    private val submitVoteUseCase: SubmitVoteUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentPollsUiState>(StudentPollsUiState.Loading)
    val uiState: StateFlow<StudentPollsUiState> = _uiState.asStateFlow()

    private var courseId: String = ""

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observeLatestPollUseCase(courseId)
                .flatMapLatest { poll ->
                    if (poll == null) {
                        flowOf(Triple(null, emptyList(), null))
                    } else {
                        combine(
                            observePollVotesUseCase(courseId, poll.id),
                            observeMyVoteUseCase(courseId, poll.id)
                        ) { votes, myVote -> Triple(poll, votes, myVote) }
                    }
                }
                .catch { e ->
                    _uiState.value =
                        StudentPollsUiState.Error(e.message ?: "No se pudo cargar la encuesta")
                }
                .collect { (poll, votes, myVote) ->
                    _uiState.value = if (poll == null) {
                        StudentPollsUiState.NoPoll
                    } else {
                        StudentPollsUiState.Result(
                            poll = poll,
                            optionResults = buildPollOptionResults(poll, votes),
                            myOptionIndex = myVote?.optionIndex
                        )
                    }
                }
        }
    }

    fun vote(optionIndex: Int) {
        val poll = (uiState.value as? StudentPollsUiState.Result)?.poll ?: return
        viewModelScope.launch {
            submitVoteUseCase(courseId, poll, optionIndex)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "poll_voted",
                        mapOf("course_id" to courseId, "poll_id" to poll.id)
                    )
                }
                .onFailure {
                    analyticsReporter.logEvent(
                        "poll_vote_error",
                        mapOf("course_id" to courseId, "poll_id" to poll.id)
                    )
                }
        }
    }
}