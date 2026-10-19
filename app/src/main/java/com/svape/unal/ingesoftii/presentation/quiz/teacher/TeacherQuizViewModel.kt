package com.svape.unal.ingesoftii.presentation.quiz.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.CloseQuizSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.CreateQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.DeleteQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetQuestionBankUseCase
import com.svape.unal.ingesoftii.domain.usecase.LaunchQuizQuestionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveCourseQuizStandingUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveLatestQuizSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveQuizSessionAnswersUseCase
import com.svape.unal.ingesoftii.domain.usecase.ResetQuizStandingUseCase
import com.svape.unal.ingesoftii.domain.usecase.UpdateQuizQuestionUseCase
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

class TeacherQuizViewModel(
    private val getQuestionBankUseCase: GetQuestionBankUseCase,
    private val createQuizQuestionUseCase: CreateQuizQuestionUseCase,
    private val updateQuizQuestionUseCase: UpdateQuizQuestionUseCase,
    private val deleteQuizQuestionUseCase: DeleteQuizQuestionUseCase,
    private val launchQuizQuestionUseCase: LaunchQuizQuestionUseCase,
    private val closeQuizSessionUseCase: CloseQuizSessionUseCase,
    private val observeLatestQuizSessionUseCase: ObserveLatestQuizSessionUseCase,
    private val observeQuizSessionAnswersUseCase: ObserveQuizSessionAnswersUseCase,
    private val observeCourseQuizStandingUseCase: ObserveCourseQuizStandingUseCase,
    private val resetQuizStandingUseCase: ResetQuizStandingUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherQuizUiState>(TeacherQuizUiState.Loading)
    val uiState: StateFlow<TeacherQuizUiState> = _uiState.asStateFlow()

    private var courseId: String = ""
    private var bank: List<QuizQuestion> = emptyList()
    private var standing: List<QuizStandingEntry> = emptyList()
    private var standingJob: Job? = null

    fun start(courseId: String) {
        this.courseId = courseId

        standingJob?.cancel()
        standingJob = viewModelScope.launch {
            observeCourseQuizStandingUseCase(courseId)
                .catch { emit(emptyList()) }
                .collect { entries ->
                    standing = entries
                    val current = _uiState.value
                    if (current is TeacherQuizUiState.Content) {
                        _uiState.value = current.copy(standing = entries)
                    }
                }
        }

        viewModelScope.launch {
            loadBank()

            observeLatestQuizSessionUseCase(courseId, withAnswerKey = true)
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf(null to emptyList())
                    } else {
                        observeQuizSessionAnswersUseCase(courseId, session.id)
                            .map { answers -> session to answers }
                    }
                }
                .catch { e ->
                    _uiState.value =
                        TeacherQuizUiState.Error(e.message ?: "No se pudo cargar el quiz")
                }
                .collect { (session, answers) ->
                    val leaderboard =
                        session?.let { buildQuizLeaderboard(it.correctOptionIndex, answers) }
                            .orEmpty()
                    _uiState.value = TeacherQuizUiState.Content(
                        bank = bank,
                        activeSession = session,
                        leaderboard = leaderboard,
                        standing = standing
                    )
                }
        }
    }

    private suspend fun loadBank() {
        getQuestionBankUseCase(courseId)
            .onSuccess { questions ->
                bank = questions
                val current = _uiState.value
                _uiState.value = if (current is TeacherQuizUiState.Content) {
                    current.copy(bank = questions)
                } else {
                    TeacherQuizUiState.Content(
                        bank = questions,
                        activeSession = null,
                        leaderboard = emptyList(),
                        standing = standing
                    )
                }
            }
            .onFailure { error ->
                _uiState.value = TeacherQuizUiState.Error(
                    error.message ?: "No se pudo cargar el banco de preguntas"
                )
            }
    }

    fun createQuestion(text: String, options: List<String>, correctOptionIndex: Int) {
        viewModelScope.launch {
            createQuizQuestionUseCase(courseId, text, options, correctOptionIndex)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_question_created",
                        mapOf("course_id" to courseId)
                    )
                    loadBank()
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo crear la pregunta")
                }
        }
    }

    fun updateQuestion(
        questionId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ) {
        viewModelScope.launch {
            updateQuizQuestionUseCase(courseId, questionId, text, options, correctOptionIndex)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_question_updated",
                        mapOf("course_id" to courseId)
                    )
                    loadBank()
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo editar la pregunta")
                }
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            deleteQuizQuestionUseCase(courseId, questionId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_question_deleted",
                        mapOf("course_id" to courseId)
                    )
                    loadBank()
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo eliminar la pregunta")
                }
        }
    }

    fun launchQuestion(question: QuizQuestion, durationSeconds: Int) {
        viewModelScope.launch {
            launchQuizQuestionUseCase(courseId, question, durationSeconds)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_question_launched",
                        mapOf("course_id" to courseId, "question_id" to question.id)
                    )
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo lanzar la pregunta")
                }
        }
    }

    fun closeActiveSession() {
        val session = (uiState.value as? TeacherQuizUiState.Content)?.activeSession ?: return
        if (!session.isActive) return
        viewModelScope.launch {
            closeQuizSessionUseCase(courseId, session.id)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_session_closed",
                        mapOf("course_id" to courseId, "session_id" to session.id)
                    )
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo cerrar la pregunta")
                }
        }
    }

    fun resetStanding() {
        viewModelScope.launch {
            resetQuizStandingUseCase(courseId)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "quiz_standing_reset",
                        mapOf("course_id" to courseId)
                    )
                }
                .onFailure { error ->
                    _uiState.value =
                        TeacherQuizUiState.Error(error.message ?: "No se pudo reiniciar el ranking")
                }
        }
    }
}
