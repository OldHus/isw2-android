package com.svape.unal.ingesoftii.presentation.quiz.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.ObserveCourseQuizStandingUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestQuizSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveMyQuizAnswerUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveQuizSessionAnswersUseCase
import com.svape.unal.ingesoftii.domain.usecase.SubmitQuizAnswerUseCase
import com.svape.unal.ingesoftii.presentation.quiz.QuizLeaderboardEntry
import com.svape.unal.ingesoftii.presentation.quiz.buildQuizLeaderboard
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class StudentQuizViewModel(
    private val observeLatestQuizSessionUseCase: ObserveLatestQuizSessionUseCase,
    private val observeMyQuizAnswerUseCase: ObserveMyQuizAnswerUseCase,
    private val observeQuizSessionAnswersUseCase: ObserveQuizSessionAnswersUseCase,
    private val submitQuizAnswerUseCase: SubmitQuizAnswerUseCase,
    private val observeCourseQuizStandingUseCase: ObserveCourseQuizStandingUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentQuizUiState>(StudentQuizUiState.Loading)
    val uiState: StateFlow<StudentQuizUiState> = _uiState.asStateFlow()

    private val _standing = MutableStateFlow<List<QuizStandingEntry>>(emptyList())
    val standing: StateFlow<List<QuizStandingEntry>> = _standing.asStateFlow()

    private val _questionRanking = MutableStateFlow<List<QuizLeaderboardEntry>>(emptyList())
    val questionRanking: StateFlow<List<QuizLeaderboardEntry>> = _questionRanking.asStateFlow()

    private var courseId: String = ""
    private var standingJob: Job? = null
    private var questionRankingJob: Job? = null

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            observeLatestQuizSessionUseCase(courseId)
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf(null to null)
                    } else {
                        observeMyQuizAnswerUseCase(courseId, session.id).map { myAnswer -> session to myAnswer }
                    }
                }
                .catch { e ->
                    _uiState.value = StudentQuizUiState.Error(e.message ?: "No se pudo cargar el quiz")
                }
                .collect { (session, myAnswer) ->
                    _uiState.value = if (session == null) {
                        StudentQuizUiState.NoActiveQuestion
                    } else {
                        StudentQuizUiState.Question(session, myAnswer)
                    }
                }
        }

        questionRankingJob?.cancel()
        questionRankingJob = viewModelScope.launch {
            observeLatestQuizSessionUseCase(courseId)
                .flatMapLatest { session ->
                    if (session == null || session.isActive) {
                        flowOf(emptyList<QuizLeaderboardEntry>())
                    } else {
                        observeQuizSessionAnswersUseCase(courseId, session.id)
                            .map { answers -> buildQuizLeaderboard(session.correctOptionIndex, answers) }
                            .catch { emit(emptyList()) }
                    }
                }
                .catch { emit(emptyList()) }
                .collect { ranking -> _questionRanking.value = ranking }
        }

        standingJob?.cancel()
        standingJob = viewModelScope.launch {
            observeCourseQuizStandingUseCase(courseId)
                .catch { emit(emptyList()) }
                .collect { entries -> _standing.value = entries }
        }
    }

    fun answer(optionIndex: Int) {
        val state = uiState.value as? StudentQuizUiState.Question ?: return
        if (state.myAnswer != null) return
        viewModelScope.launch {
            submitQuizAnswerUseCase(courseId, state.session, optionIndex)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_answer_submitted",
                        mapOf("course_id" to courseId, "session_id" to state.session.id)
                    )
                }
                .onFailure {
                    analyticsReporter.logEvent(
                        "quiz_answer_error",
                        mapOf("course_id" to courseId, "session_id" to state.session.id)
                    )
                }
        }
    }
}