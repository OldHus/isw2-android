package com.svape.unal.ingesoftii.presentation.attendance.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter
import com.svape.unal.ingesoftii.domain.usecase.CloseAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.GetCourseStudentsUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveActiveAttendanceSessionUseCase
import com.svape.unal.ingesoftii.domain.usecase.ObserveAttendanceRegistrationsUseCase
import com.svape.unal.ingesoftii.domain.usecase.StartAttendanceSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherAttendanceViewModel(
    private val observeActiveAttendanceSessionUseCase: ObserveActiveAttendanceSessionUseCase,
    private val observeAttendanceRegistrationsUseCase: ObserveAttendanceRegistrationsUseCase,
    private val startAttendanceSessionUseCase: StartAttendanceSessionUseCase,
    private val closeAttendanceSessionUseCase: CloseAttendanceSessionUseCase,
    private val getCourseStudentsUseCase: GetCourseStudentsUseCase,
    private val analyticsReporter: AnalyticsReporter
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TeacherAttendanceUiState>(TeacherAttendanceUiState.Loading)
    val uiState: StateFlow<TeacherAttendanceUiState> = _uiState.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    private var courseId: String = ""
    private var courseStudents: List<CourseStudent> = emptyList()

    fun start(courseId: String) {
        this.courseId = courseId
        viewModelScope.launch {
            courseStudents = getCourseStudentsUseCase(courseId).getOrElse {
                _uiState.value = TeacherAttendanceUiState.Error(
                    it.message ?: "No se pudieron cargar los estudiantes del curso"
                )
                return@launch
            }

            observeActiveAttendanceSessionUseCase(courseId, includeCode = true)
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf(null to emptyList())
                    } else {
                        observeAttendanceRegistrationsUseCase(courseId, session.id)
                            .map { records -> session to records }
                    }
                }
                .catch { e ->
                    _uiState.value = TeacherAttendanceUiState.Error(
                        e.message ?: "No se pudo cargar la sesión de asistencia"
                    )
                }
                .collect { (session, records) ->
                    _isStarting.value = false
                    _uiState.value = if (session == null) {
                        TeacherAttendanceUiState.NoActiveSession
                    } else {
                        val entries = records
                            .sortedBy { it.registeredAt }
                            .map { record ->
                                val student = courseStudents.find { it.uid == record.studentUid }
                                AttendanceEntry(
                                    studentName = student?.name?.ifBlank { null }
                                        ?: student?.email?.ifBlank { null }
                                        ?: record.studentUid,
                                    studentEmail = student?.email.orEmpty(),
                                    registeredAtMillis = record.registeredAt,
                                    photoUrl = student?.photoUrl?.ifBlank { null },
                                )
                            }
                        TeacherAttendanceUiState.Active(
                            session = session,
                            entries = entries,
                            totalStudents = courseStudents.size,
                        )
                    }
                }
        }
    }

    fun startSession(code: String, durationMinutes: Int) {
        if (_isStarting.value) return
        _isStarting.value = true
        viewModelScope.launch {
            startAttendanceSessionUseCase(courseId, code, durationMinutes)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "attendance_session_started",
                        mapOf("course_id" to courseId, "duration_minutes" to durationMinutes)
                    )
                }
                .onFailure { error ->
                    _isStarting.value = false
                    _uiState.value = TeacherAttendanceUiState.Error(
                        error.message ?: "No se pudo iniciar la sesión"
                    )
                }
        }
    }

    fun closeActiveSession() {
        val currentSession = (uiState.value as? TeacherAttendanceUiState.Active)?.session ?: return
        viewModelScope.launch {
            closeAttendanceSessionUseCase(courseId, currentSession.id)
                .onSuccess {
                    analyticsReporter.logEvent(
                        "attendance_session_closed",
                        mapOf("course_id" to courseId, "session_id" to currentSession.id)
                    )
                }
                .onFailure { error ->
                    _uiState.value = TeacherAttendanceUiState.Error(
                        error.message ?: "No se pudo cerrar la sesión"
                    )
                }
        }
    }

    fun buildExportText(): String {
        val state = uiState.value as? TeacherAttendanceUiState.Active ?: return ""
        if (state.entries.isEmpty()) return "Ningún estudiante registró asistencia todavía."

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val header = "Asistencia — código ${state.session.code}\n"
        val lines = state.entries.joinToString("\n") { entry ->
            val time = timeFormat.format(Date(entry.registeredAtMillis))
            "${entry.studentName} - ${entry.studentEmail} - $time"
        }
        return header + lines
    }
}