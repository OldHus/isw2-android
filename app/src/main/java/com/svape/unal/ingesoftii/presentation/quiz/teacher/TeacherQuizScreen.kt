package com.svape.unal.ingesoftii.presentation.quiz.teacher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.presentation.quiz.AnswerDistribution
import com.svape.unal.ingesoftii.presentation.quiz.LeaderboardRow
import com.svape.unal.ingesoftii.presentation.quiz.MAX_QUIZ_OPTIONS
import com.svape.unal.ingesoftii.presentation.quiz.OptionBadge
import com.svape.unal.ingesoftii.presentation.quiz.QuizLeaderboardEntry
import com.svape.unal.ingesoftii.presentation.quiz.QuizLiveBadge
import com.svape.unal.ingesoftii.presentation.quiz.QuizPodium
import com.svape.unal.ingesoftii.presentation.quiz.QuizShapeIcon
import com.svape.unal.ingesoftii.presentation.quiz.QuizStandingCard
import com.svape.unal.ingesoftii.presentation.quiz.QuizTimer
import com.svape.unal.ingesoftii.presentation.quiz.optionLetter
import com.svape.unal.ingesoftii.presentation.quiz.quizOptionStyle
import com.svape.unal.ingesoftii.presentation.quiz.rememberRemainingMillis
import com.svape.unal.ingesoftii.presentation.quiz.rememberSessionTotalMillis
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import kotlinx.coroutines.launch

private const val DEFAULT_DURATION_SECONDS = 20
private val DURATION_PRESETS = listOf(10, 20, 30, 60, 120)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherQuizScreen(
    courseId: String,
    viewModel: TeacherQuizViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditor by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var launchingQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var questionToDelete by remember { mutableStateOf<QuizQuestion?>(null) }
    var showProjector by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    var lastContent by remember { mutableStateOf<TeacherQuizUiState.Content?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeacherQuizUiState.Content -> lastContent = state
            is TeacherQuizUiState.Error -> if (lastContent != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val content = (uiState as? TeacherQuizUiState.Content) ?: lastContent
    val session = content?.activeSession
    val liveNow = session?.isActive == true

    fun openEditor(question: QuizQuestion?) {
        editingQuestion = question
        showEditor = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Quiz en vivo",
                subtitle = content?.let {
                    val n = it.bank.size
                    if (n == 1) "1 pregunta en el banco" else "$n preguntas en el banco"
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (content != null && content.bank.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Nueva pregunta") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    expanded = fabExpanded,
                    onClick = { openEditor(null) },
                )
            }
        },
    ) { padding ->
        val state = uiState
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                content != null -> {
                    val filtered = remember(content.bank, query) {
                        val q = query.trim()
                        if (q.isEmpty()) content.bank
                        else content.bank.filter { question ->
                            question.text.contains(q, true) || question.options.any { it.contains(q, true) }
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (session != null) {
                            item(key = "live_${session.id}") {
                                LiveSessionCard(
                                    session = session,
                                    leaderboard = content.leaderboard,
                                    onExpired = { viewModel.closeActiveSession() },
                                    onClose = { viewModel.closeActiveSession() },
                                    onProject = { showProjector = true },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }

                        if (content.standing.isNotEmpty()) {
                            item(key = "standing") {
                                QuizStandingCard(
                                    standing = content.standing,
                                    modifier = Modifier.animateItem(),
                                    action = {
                                        TextButton(
                                            onClick = { showResetConfirm = true },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error,
                                            ),
                                        ) {
                                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Reiniciar")
                                        }
                                    },
                                )
                            }
                        }

                        item(key = "bank_header") {
                            SectionHeader(
                                "Banco de preguntas (${content.bank.size})",
                                Modifier.padding(top = if (session != null || content.standing.isNotEmpty()) 8.dp else 0.dp),
                            )
                        }

                        if (content.bank.size > 4) {
                            item(key = "search") {
                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Buscar pregunta u opción") },
                                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (query.isNotEmpty()) {
                                            IconButton(onClick = { query = "" }) {
                                                Icon(Icons.Rounded.Close, contentDescription = "Limpiar búsqueda")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = CircleShape,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    ),
                                )
                            }
                        }

                        if (content.bank.isEmpty()) {
                            item(key = "empty") {
                                EmptyState(
                                    icon = Icons.Outlined.Quiz,
                                    title = "Arma tu primer quiz",
                                    message = "Crea preguntas de opción múltiple y lánzalas en clase. " +
                                            "Tus estudiantes responden en vivo y ganan más puntos si aciertan rápido.",
                                    action = {
                                        Button(onClick = { openEditor(null) }) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Crear pregunta")
                                        }
                                    },
                                )
                            }
                        } else if (filtered.isEmpty()) {
                            item(key = "no_results") {
                                Text(
                                    "Sin resultados para \"$query\".",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }

                        itemsIndexed(filtered, key = { _, q -> q.id }) { index, question ->
                            StaggeredEnter(index = index, modifier = Modifier.animateItem()) {
                                QuestionBankCard(
                                    question = question,
                                    number = content.bank.indexOf(question) + 1,
                                    launchEnabled = !liveNow,
                                    onLaunch = { launchingQuestion = question },
                                    onEdit = { openEditor(question) },
                                    onDelete = { questionToDelete = question },
                                )
                            }
                        }
                    }
                }

                state is TeacherQuizUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar el quiz",
                        message = state.message,
                        action = { TextButton(onClick = { viewModel.start(courseId) }) { Text("Reintentar") } },
                    )
                }

                else -> LoadingList(header = true, rows = 4, rowHeight = 140.dp)
            }
        }
    }

    if (showEditor) {
        QuestionEditorSheet(
            initial = editingQuestion,
            onDismiss = { showEditor = false },
            onConfirm = { text, options, correctIndex ->
                val current = editingQuestion
                if (current == null) {
                    viewModel.createQuestion(text, options, correctIndex)
                    scope.launch { snackbarHostState.showSnackbar("Pregunta agregada al banco") }
                } else {
                    viewModel.updateQuestion(current.id, text, options, correctIndex)
                    scope.launch { snackbarHostState.showSnackbar("Pregunta actualizada") }
                }
                showEditor = false
            },
        )
    }

    launchingQuestion?.let { question ->
        LaunchQuestionDialog(
            question = question,
            onDismiss = { launchingQuestion = null },
            onConfirm = { seconds ->
                viewModel.launchQuestion(question, seconds)
                launchingQuestion = null
                scope.launch {
                    listState.animateScrollToItem(0)
                    snackbarHostState.showSnackbar("¡Pregunta lanzada! Tus estudiantes ya pueden responder")
                }
            },
        )
    }

    questionToDelete?.let { question ->
        val colors = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
            title = { Text("¿Eliminar pregunta?") },
            text = { Text("\"${question.text}\" se quitará del banco. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(question.id)
                        questionToDelete = null
                        scope.launch { snackbarHostState.showSnackbar("Pregunta eliminada") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { questionToDelete = null }) { Text("Cancelar") } },
        )
    }

    if (showResetConfirm) {
        val colors = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = { Icon(Icons.Outlined.RestartAlt, contentDescription = null, tint = colors.error) },
            title = { Text("¿Reiniciar el ranking acumulado?") },
            text = {
                Text(
                    "Se eliminarán todas las preguntas lanzadas y las respuestas de tus estudiantes, y el ranking " +
                            "quedará vacío. Esta acción no se puede deshacer. Tu banco de preguntas no se borra.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStanding()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) { Text("Reiniciar") }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("Cancelar") } },
        )
    }

    if (showProjector && session != null && content != null) {
        ProjectorDialog(
            session = session,
            leaderboard = content.leaderboard,
            onDismiss = { showProjector = false },
        )
    }
}


@Composable
private fun LiveSessionCard(
    session: QuizSession,
    leaderboard: List<QuizLeaderboardEntry>,
    onExpired: () -> Unit,
    onClose: () -> Unit,
    onProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val remaining = rememberRemainingMillis(session.expiresAt, session.isActive)
    val total = rememberSessionTotalMillis(session.id, session.expiresAt)
    val counts = remember(leaderboard, session.options) {
        List(session.options.size) { i -> leaderboard.count { it.selectedOptionIndex == i } }
    }
    val correct = leaderboard.count { it.isCorrect }

    val currentOnExpired by rememberUpdatedState(onExpired)
    LaunchedEffect(session.id, session.isActive, remaining == 0L) {
        if (session.isActive && remaining == 0L) currentOnExpired()
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ext.card,
        border = BorderStroke(if (session.isActive) 2.dp else 1.dp, if (session.isActive) colors.primary else ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    QuizLiveBadge(active = session.isActive)
                    Spacer(Modifier.height(10.dp))
                    Text(session.questionText, style = MaterialTheme.typography.titleLarge)
                }
                if (session.isActive) {
                    Spacer(Modifier.width(12.dp))
                    QuizTimer(remainingMillis = remaining, totalMillis = total)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("${leaderboard.size}", if (leaderboard.size == 1) "respuesta" else "respuestas")
                StatChip("$correct", if (correct == 1) "correcta" else "correctas", ext.success)
            }

            Spacer(Modifier.height(16.dp))
            AnswerDistribution(
                options = session.options,
                counts = counts,
                correctIndex = session.correctOptionIndex ?: -1,
                revealCorrect = session.correctOptionIndex != null,
            )

            if (leaderboard.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = colors.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Leaderboard, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (session.isActive) "Ranking parcial" else "Ranking final",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(Modifier.height(12.dp))
                if (!session.isActive && leaderboard.isNotEmpty()) {
                    QuizPodium(leaderboard)
                    Spacer(Modifier.height(8.dp))
                    leaderboard.drop(3).forEach { LeaderboardRow(it) }
                } else {
                    leaderboard.forEach { LeaderboardRow(it) }
                }
            } else if (session.isActive) {
                Text(
                    "Esperando respuestas…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onProject, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Slideshow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proyectar")
                }
                if (session.isActive) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.6f)),
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

@Composable
private fun StatChip(value: String, label: String, accent: Color = MaterialTheme.colorScheme.primary) {
    Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
private fun QuestionBankCard(
    question: QuizQuestion,
    number: Int,
    launchEnabled: Boolean,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(shape = MaterialTheme.shapes.small, color = colors.primaryContainer) {
                    Text(
                        "P$number",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    question.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp),
                )
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones de la pregunta", tint = colors.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = colors.error) },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEachIndexed { index, option ->
                    val isCorrect = index == question.correctOptionIndex
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OptionBadge(index, size = 22.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCorrect) colors.onSurface else colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (isCorrect) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = "Correcta",
                                tint = ext.success,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!launchEnabled) {
                    Text(
                        "Cierra la pregunta en vivo para lanzar otra",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Button(onClick = onLaunch, enabled = launchEnabled) {
                    Icon(Icons.Outlined.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lanzar")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditorSheet(
    initial: QuizQuestion?,
    onDismiss: () -> Unit,
    onConfirm: (text: String, options: List<String>, correctIndex: Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    var text by rememberSaveable { mutableStateOf(initial?.text.orEmpty()) }
    val options = remember { mutableStateListOf<String>().apply { addAll(initial?.options ?: listOf("", "", "", "")) } }
    var correctIndex by rememberSaveable { mutableIntStateOf(initial?.correctOptionIndex ?: 0) }
    var showDiscard by remember { mutableStateOf(false) }

    val filledOptions = options.count { it.isNotBlank() }
    val problem = when {
        text.isBlank() -> "Escribe la pregunta"
        filledOptions < 2 -> "Agrega al menos 2 opciones"
        options.getOrNull(correctIndex).isNullOrBlank() -> "Marca con ✓ la opción correcta"
        else -> null
    }
    val dirty = text != initial?.text.orEmpty() ||
            options.toList() != (initial?.options ?: listOf("", "", "", "")) ||
            correctIndex != (initial?.correctOptionIndex ?: 0)

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

    fun save() {
        val cleaned = options.mapIndexed { i, o -> i to o.trim() }.filter { it.second.isNotEmpty() }
        val newCorrect = cleaned.indexOfFirst { it.first == correctIndex }
        onConfirm(text.trim(), cleaned.map { it.second }, newCorrect)
    }

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
                Text(
                    if (initial == null) "Nueva pregunta" else "Editar pregunta",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { save() }, enabled = problem == null && (initial == null || dirty)) {
                    Text("Guardar")
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(200) },
                label = { Text("Pregunta") },
                placeholder = { Text("Ej. ¿Qué patrón separa la vista de la lógica?") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                supportingText = { Text("${text.length}/200") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            Text("Opciones", style = MaterialTheme.typography.titleSmall)
            Text(
                "Toca ✓ para marcar la respuesta correcta.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))

            options.forEachIndexed { index, value ->
                val isCorrect = index == correctIndex
                Row(
                    Modifier.padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OptionBadge(index, size = 40.dp)
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { options[index] = it.take(80) },
                        placeholder = { Text("Opción ${optionLetter(index)}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isCorrect) ext.success else colors.primary,
                            unfocusedBorderColor = if (isCorrect) ext.success else colors.outlineVariant,
                        ),
                        trailingIcon = {
                            IconButton(onClick = { correctIndex = index }) {
                                Icon(
                                    if (isCorrect) Icons.Rounded.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = if (isCorrect) "Respuesta correcta" else "Marcar como correcta",
                                    tint = if (isCorrect) ext.success else colors.onSurfaceVariant,
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (options.size > 2) {
                        IconButton(onClick = {
                            options.removeAt(index)
                            correctIndex = when {
                                index == correctIndex -> 0
                                index < correctIndex -> correctIndex - 1
                                else -> correctIndex
                            }
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar opción ${optionLetter(index)}", tint = colors.onSurfaceVariant)
                        }
                    }
                }
            }

            if (options.size < MAX_QUIZ_OPTIONS) {
                TextButton(onClick = { options.add("") }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar opción")
                }
            }

            if (problem != null) {
                Text(
                    problem,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("¿Descartar cambios?") },
            text = { Text("Perderás lo que escribiste en esta pregunta.") },
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LaunchQuestionDialog(
    question: QuizQuestion,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var selected by rememberSaveable { mutableIntStateOf(DEFAULT_DURATION_SECONDS) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var customText by rememberSaveable { mutableStateOf("") }
    val seconds = if (custom) customText.toIntOrNull() else selected
    val valid = seconds != null && seconds in 1..300

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.RocketLaunch, contentDescription = null) },
        title = { Text("Lanzar pregunta") },
        text = {
            Column {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        question.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Tiempo para responder", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DURATION_PRESETS.forEach { preset ->
                        FilterChip(
                            selected = !custom && selected == preset,
                            onClick = {
                                custom = false
                                selected = preset
                            },
                            label = { Text(if (preset < 60) "$preset s" else "${preset / 60} min") },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                    FilterChip(
                        selected = custom,
                        onClick = { custom = true },
                        label = { Text("Otro") },
                    )
                }
                if (custom) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Segundos (1 a 300)") },
                        singleLine = true,
                        isError = customText.isNotEmpty() && !valid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Quien acierte más rápido gana más puntos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = { seconds?.let(onConfirm) }, enabled = valid) { Text("Lanzar ahora") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun ProjectorDialog(
    session: QuizSession,
    leaderboard: List<QuizLeaderboardEntry>,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val remaining = rememberRemainingMillis(session.expiresAt, session.isActive)
    val total = rememberSessionTotalMillis(session.id, session.expiresAt)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        DisposableEffect(Unit) {
            view.keepScreenOn = true
            onDispose { view.keepScreenOn = false }
        }
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuizLiveBadge(active = session.isActive)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${leaderboard.size} " + if (leaderboard.size == 1) "respuesta" else "respuestas",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Salir del modo proyector") }
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        session.questionText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (session.isActive) {
                        Spacer(Modifier.width(16.dp))
                        QuizTimer(remainingMillis = remaining, totalMillis = total, size = 112.dp, strokeWidth = 10.dp)
                    }
                }
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = session.isActive,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "projectorPhase",
                ) { active ->
                    if (active) {
                        ProjectorOptionsGrid(session.options, revealCorrect = null)
                    } else {
                        Column {
                            ProjectorOptionsGrid(session.options, revealCorrect = session.correctOptionIndex)
                            if (leaderboard.isNotEmpty()) {
                                Spacer(Modifier.height(28.dp))
                                Text(
                                    "Podio",
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(12.dp))
                                QuizPodium(leaderboard)
                                if (leaderboard.size > 3) {
                                    Spacer(Modifier.height(16.dp))
                                    leaderboard.drop(3).forEach { LeaderboardRow(it) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectorOptionsGrid(options: List<String>, revealCorrect: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEachIndexed { col, option ->
                    val index = row * 2 + col
                    val style = quizOptionStyle(index)
                    val dim = revealCorrect != null && revealCorrect != index
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = if (dim) style.color.copy(alpha = 0.25f) else style.color,
                        contentColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 96.dp),
                    ) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            QuizShapeIcon(style.shape, Modifier.size(32.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(
                                option,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            if (revealCorrect == index) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "Correcta", modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}