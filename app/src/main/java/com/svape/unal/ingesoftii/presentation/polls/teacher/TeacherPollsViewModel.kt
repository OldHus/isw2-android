package com.svape.unal.ingesoftii.presentation.polls.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.ClosePollUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestPollUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObservePollVotesUseCase
import com.svape.unal.ingesoftii.domain.usecase.StartPollUseCase
import com.svape.unal.ingesoftii.presentation.polls.buildPollOptionResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TeacherPollsViewModel(
    private val observeLatestPollUseCase: ObserveLatestPollUseCase,
    private val observePollVotesUseCase: ObservePollVotesUseCase,
    private val startPollUseCase: StartPollUseCase,
    private val closePollUseCase: ClosePollUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherPollsUiState>(TeacherPollsUiState.Loading)
    val uiState: StateFlow<TeacherPollsUiState> = _uiState.asStateFlow()

    private var courseId: String = ""

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observeLatestPollUseCase(courseId)
                .flatMapLatest { poll ->
                    if (poll == null) {
                        flowOf(null to emptyList())
                    } else {
                        observePollVotesUseCase(courseId, poll.id).map { votes -> poll to votes }
                    }
                }
                .catch { e ->
                    _uiState.value =
                        TeacherPollsUiState.Error(e.message ?: "No se pudo cargar la encuesta")
                }
                .collect { (poll, votes) ->
                    _uiState.value = if (poll == null) {
                        TeacherPollsUiState.NoPoll
                    } else {
                        TeacherPollsUiState.Result(poll, buildPollOptionResults(poll, votes))
                    }
                }
        }
    }

    fun startPoll(question: String, options: List<String>, expiresAtMillis: Long) {
        viewModelScope.launch {
            startPollUseCase(courseId, question, options, expiresAtMillis)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "poll_started",
                        mapOf("course_id" to courseId)
                    )
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherPollsUiState.Error(error.message ?: "No se pudo crear la encuesta")
                }
        }
    }

    fun closeActivePoll() {
        val currentPoll = (uiState.value as? TeacherPollsUiState.Result)?.poll ?: return
        if (!currentPoll.isActive) return
        closePoll(currentPoll)
    }

    fun closeExpiredPoll() {
        val currentPoll = (uiState.value as? TeacherPollsUiState.Result)?.poll ?: return
        val expired = currentPoll.expiresAt > 0L &&
                System.currentTimeMillis() >= currentPoll.expiresAt
        if (!currentPoll.isActive || !expired) return
        closePoll(currentPoll)
    }

    private fun closePoll(poll: Poll) {
        viewModelScope.launch {
            closePollUseCase(courseId, poll.id)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "poll_closed",
                        mapOf("course_id" to courseId, "poll_id" to poll.id)
                    )
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherPollsUiState.Error(error.message ?: "No se pudo cerrar la encuesta")
                }
        }
    }
}