package com.svape.unal.ingesoftii.presentation.polls.teacher

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToVote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.presentation.polls.PollHeaderCard
import com.svape.unal.ingesoftii.presentation.polls.PollOptionRow
import com.svape.unal.ingesoftii.presentation.polls.VotersSheet
import com.svape.unal.ingesoftii.presentation.polls.formatPollDeadline
import com.svape.unal.ingesoftii.presentation.polls.leadingOptionIndex
import com.svape.unal.ingesoftii.presentation.polls.rememberPollRemaining
import com.svape.unal.ingesoftii.presentation.polls.sharePollResults
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

private const val MAX_POLL_OPTIONS = 10

private data class DurationPreset(val label: String, val minutes: Long?)

private val DURATION_PRESETS = listOf(
    DurationPreset("15 min", 15),
    DurationPreset("1 hora", 60),
    DurationPreset("1 día", 60 * 24),
    DurationPreset("3 días", 60 * 24 * 3),
    DurationPreset("1 semana", 60 * 24 * 7),
    DurationPreset("Elegir fecha", null),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPollsScreen(
    courseId: String,
    viewModel: TeacherPollsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showComposer by remember { mutableStateOf(false) }
    var confirmClose by remember { mutableStateOf(false) }
    var votersForOption by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    var lastGood by remember { mutableStateOf<TeacherPollsUiState?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeacherPollsUiState.Result, TeacherPollsUiState.NoPoll -> lastGood = state
            is TeacherPollsUiState.Error -> if (lastGood != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val shown = when (val state = uiState) {
        is TeacherPollsUiState.Error -> lastGood ?: state
        TeacherPollsUiState.Loading -> lastGood ?: state
        else -> state
    }
    val result = shown as? TeacherPollsUiState.Result
    val hasActivePoll = result?.poll?.isActive == true

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Encuestas",
                subtitle = when {
                    hasActivePoll -> "Encuesta en curso"
                    result != null -> "Última encuesta"
                    else -> null
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (result != null) {
                        IconButton(onClick = { sharePollResults(context, result.poll, result.optionResults) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Compartir resultados")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (result != null && !hasActivePoll) {
                ExtendedFloatingActionButton(
                    text = { Text("Nueva encuesta") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = { showComposer = true },
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = shown) {
                TeacherPollsUiState.Loading -> LoadingList(header = true, rows = 4, rowHeight = 84.dp)

                is TeacherPollsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar la encuesta",
                        message = state.message,
                        action = { TextButton(onClick = { viewModel.start(courseId) }) { Text("Reintentar") } },
                    )
                }

                TeacherPollsUiState.NoPoll -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.Poll,
                        title = "Pregúntale algo a tu clase",
                        message = "Crea una encuesta para decidir fechas, temas o medir cómo va el curso. " +
                                "Verás los votos en tiempo real.",
                        action = {
                            Button(onClick = { showComposer = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Crear encuesta")
                            }
                        },
                    )
                }

                is TeacherPollsUiState.Result -> {
                    val poll = state.poll
                    val remaining = rememberPollRemaining(poll.expiresAt, poll.isActive)
                    val totalVotes = state.optionResults.sumOf { it.voterNames.size }
                    val leader = leadingOptionIndex(state.optionResults)

                    LaunchedEffect(poll.id, poll.isActive, remaining == 0L) {
                        val reallyExpired = poll.expiresAt > 0L &&
                                System.currentTimeMillis() >= poll.expiresAt
                        if (poll.isActive && remaining == 0L && reallyExpired) {
                            viewModel.closeExpiredPoll()
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item(key = "header_${poll.id}") {
                            PollHeaderCard(poll = poll, remainingMillis = remaining, totalVotes = totalVotes)
                        }
                        item(key = "results_header") {
                            SectionHeader(
                                if (poll.isActive) "Resultados en vivo" else "Resultados finales",
                                Modifier.padding(top = 8.dp),
                            )
                        }
                        itemsIndexed(state.optionResults, key = { index, _ -> "${poll.id}_$index" }) { index, option ->
                            StaggeredEnter(index = index) {
                                PollOptionRow(
                                    result = option,
                                    totalVotes = totalVotes,
                                    isMine = false,
                                    isLeader = index == leader,
                                    closed = !poll.isActive,
                                    showResults = true,
                                    enabled = option.voterNames.isNotEmpty(),
                                    onClick = { votersForOption = index },
                                    onShowVoters = { votersForOption = index },
                                )
                            }
                        }
                        item(key = "actions") {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    FilledTonalButton(
                                        onClick = { votersForOption = leader.coerceAtLeast(0) },
                                        enabled = totalVotes > 0,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ver votantes")
                                    }
                                    FilledTonalButton(
                                        onClick = { sharePollResults(context, poll, state.optionResults) },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Compartir .txt")
                                    }
                                }
                                if (poll.isActive) {
                                    OutlinedButton(
                                        onClick = { confirmClose = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                    ) {
                                        Icon(Icons.Outlined.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Cerrar ahora")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showComposer) {
        PollComposerSheet(
            onDismiss = { showComposer = false },
            onConfirm = { question, options, expiresAt ->
                viewModel.startPoll(question, options, expiresAt)
                showComposer = false
                scope.launch { snackbarHostState.showSnackbar("¡Encuesta publicada! Cierra ${formatPollDeadline(expiresAt)}") }
            },
        )
    }

    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            icon = { Icon(Icons.Outlined.StopCircle, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("¿Cerrar la encuesta ahora?") },
            text = { Text("Nadie más podrá votar y los resultados quedarán como finales.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeActivePoll()
                        confirmClose = false
                        scope.launch { snackbarHostState.showSnackbar("Encuesta cerrada") }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text("Cerrar") }
            },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Cancelar") } },
        )
    }

    val option = votersForOption
    if (option != null && result != null) {
        VotersSheet(results = result.optionResults, initialOption = option, onDismiss = { votersForOption = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PollComposerSheet(
    onDismiss: () -> Unit,
    onConfirm: (question: String, options: List<String>, expiresAt: Long) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val context = LocalContext.current
    var question by rememberSaveable { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var presetIndex by rememberSaveable { mutableIntStateOf(2) }
    var customExpiresAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateUtc by remember { mutableStateOf<Long?>(null) }
    var showDiscard by remember { mutableStateOf(false) }

    val preset = DURATION_PRESETS[presetIndex]
    fun resolveExpiresAt(): Long? =
        preset.minutes?.let { System.currentTimeMillis() + it * 60_000 } ?: customExpiresAt

    val previewExpiresAt = resolveExpiresAt()
    val filled = options.count { it.isNotBlank() }
    val problem = when {
        question.isBlank() -> "Escribe la pregunta"
        filled < 2 -> "Agrega al menos 2 opciones"
        previewExpiresAt == null -> "Elige la fecha y hora de cierre"
        previewExpiresAt <= System.currentTimeMillis() -> "La hora de cierre ya pasó"
        else -> null
    }
    val dirty = question.isNotBlank() || options.any { it.isNotBlank() }

    val currentDirty by rememberUpdatedState(dirty)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (value == SheetValue.Hidden && currentDirty) {
                showDiscard = true
                false
            } else {
                true
            }
        },
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nueva encuesta", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val expiresAt = resolveExpiresAt() ?: return@Button
                        onConfirm(question.trim(), options.map { it.trim() }.filter { it.isNotEmpty() }, expiresAt)
                    },
                    enabled = problem == null,
                ) {
                    Icon(Icons.Outlined.HowToVote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Publicar")
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it.take(200) },
                label = { Text("Pregunta") },
                placeholder = { Text("Ej. ¿Qué día prefieren para el parcial?") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text("Opciones", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            options.forEachIndexed { index, value ->
                Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = colors.secondaryContainer, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { options[index] = it.take(80) },
                        placeholder = { Text("Opción ${index + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                    )
                    if (options.size > 2) {
                        IconButton(onClick = { options.removeAt(index) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar opción ${index + 1}", tint = colors.onSurfaceVariant)
                        }
                    }
                }
            }
            if (options.size < MAX_POLL_OPTIONS) {
                TextButton(onClick = { options.add("") }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar opción")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("¿Cuándo cierra?", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DURATION_PRESETS.forEachIndexed { index, p ->
                    FilterChip(
                        selected = presetIndex == index,
                        onClick = {
                            presetIndex = index
                            if (p.minutes == null) showDatePicker = true
                        },
                        label = { Text(p.label) },
                        leadingIcon = if (p.minutes == null) {
                            { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (problem == "La hora de cierre ya pasó") colors.errorContainer else colors.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        previewExpiresAt?.let { "Cierra ${formatPollDeadline(it)}" } ?: "Toca \"Elegir fecha\" para definir el cierre",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (problem != null && problem != "La hora de cierre ya pasó") {
                Text(
                    problem,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Los estudiantes pueden cambiar su voto mientras la encuesta esté activa.",
                style = MaterialTheme.typography.bodySmall,
                color = ext.success,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    if (showDatePicker) {
        val todayUtc = remember { localTodayAsUtcMillis() }
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = todayUtc,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDateUtc = dateState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    },
                    enabled = dateState.selectedDateMillis != null,
                ) { Text("Siguiente") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = dateState, title = { Text("Día de cierre", Modifier.padding(start = 24.dp, top = 16.dp)) })
        }
    }

    if (showTimePicker) {
        val now = remember { Calendar.getInstance() }
        val timeState = rememberTimePickerState(
            initialHour = (now.get(Calendar.HOUR_OF_DAY) + 1) % 24,
            initialMinute = 0,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Hora de cierre") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    customExpiresAt = combineUtcDateWithLocalTime(
                        pendingDateUtc ?: localTodayAsUtcMillis(),
                        timeState.hour,
                        timeState.minute,
                    )
                    showTimePicker = false
                }) { Text("Listo") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
        )
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("¿Descartar encuesta?") },
            text = { Text("Perderás lo que escribiste.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscard = false
                    onDismiss()
                }) { Text("Descartar", color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Seguir editando") } },
        )
    }
}

private fun localTodayAsUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun combineUtcDateWithLocalTime(dateUtcMillis: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtcMillis }
    return Calendar.getInstance().apply {
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}