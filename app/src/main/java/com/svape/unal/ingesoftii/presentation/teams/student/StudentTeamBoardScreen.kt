package com.svape.unal.ingesoftii.presentation.teams.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GroupOff
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.presentation.teams.BoardHeaderCard
import com.svape.unal.ingesoftii.presentation.teams.BoardProgress
import com.svape.unal.ingesoftii.presentation.teams.KanbanBoard
import com.svape.unal.ingesoftii.presentation.teams.TaskActionButton
import com.svape.unal.ingesoftii.presentation.teams.TaskFormDialog
import com.svape.unal.ingesoftii.presentation.teams.TeamMonogram
import com.svape.unal.ingesoftii.presentation.teams.columnColor
import com.svape.unal.ingesoftii.presentation.teams.labelFor
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import kotlinx.coroutines.launch

private val studentFlow = listOf(TaskColumn.HACER, TaskColumn.DESARROLLANDO, TaskColumn.QA)

private fun previousOf(column: TaskColumn): TaskColumn? =
    studentFlow.indexOf(column).takeIf { it > 0 }?.let { studentFlow[it - 1] }

private fun nextOf(column: TaskColumn): TaskColumn? =
    studentFlow.indexOf(column).takeIf { it in 0 until studentFlow.lastIndex }?.let { studentFlow[it + 1] }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTeamBoardScreen(
    courseId: String,
    viewModel: StudentTeamBoardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTaskDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId) {
        viewModel.load(courseId)
    }

    var lastSuccess by remember { mutableStateOf<StudentTeamBoardUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is StudentTeamBoardUiState.Success -> lastSuccess = state
            is StudentTeamBoardUiState.Error -> if (lastSuccess != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val content = (uiState as? StudentTeamBoardUiState.Success) ?: lastSuccess

    val move: (KanbanTask, TaskColumn) -> Unit = { task, destination ->
        viewModel.moveTask(task.id, task.column, destination)
        scope.launch { snackbarHostState.showSnackbar("\"${task.title}\" → ${labelFor(destination)}") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = content?.team?.name ?: "Mi equipo",
                subtitle = content?.let { "Proyecto de aula · ${it.team.memberUids.size} integrantes" },
            )
        },
        floatingActionButton = {
            if (content != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Proponer tarea") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = { showCreateTaskDialog = true },
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
                content != null -> Column(Modifier.fillMaxSize()) {
                    BoardHeaderCard(Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TeamMonogram(content.team.name, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(content.team.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Propón tareas, muévelas mientras avanzan y envíalas a QA cuando estén listas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        BoardProgress(content.tasks)
                    }

                    KanbanBoard(tasks = content.tasks) { task ->
                        StudentTaskActions(task, onMove = { destination -> move(task, destination) })
                    }
                }

                state is StudentTeamBoardUiState.NoTeamAssigned -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.GroupOff,
                        title = "Aún no tienes equipo",
                        message = "Tu docente te asignará a un equipo pronto. Cuando lo haga, verás aquí el tablero del proyecto.",
                    )
                }

                state is StudentTeamBoardUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Outlined.CloudOff, title = "Algo salió mal", message = state.message)
                }

                else -> LoadingList(header = true, rows = 4, rowHeight = 96.dp)
            }
        }
    }

    if (showCreateTaskDialog) {
        TaskFormDialog(
            title = "Proponer tarea",
            confirmLabel = "Proponer",
            icon = Icons.Outlined.Lightbulb,
            onDismiss = { showCreateTaskDialog = false },
            onConfirm = { title, description ->
                viewModel.createTask(title, description)
                showCreateTaskDialog = false
                scope.launch { snackbarHostState.showSnackbar("Propuesta enviada al docente") }
            },
        )
    }
}

@Composable
private fun StudentTaskActions(task: KanbanTask, onMove: (TaskColumn) -> Unit) {
    val colors = MaterialTheme.colorScheme
    when (task.column) {
        TaskColumn.PROPUESTAS -> WaitingLabel("Esperando aprobación del docente")
        TaskColumn.APROBADA -> Unit
        TaskColumn.QA -> {
            previousOf(task.column)?.let { prev ->
                TaskActionButton(
                    text = labelFor(prev),
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = { onMove(prev) },
                    container = colors.surfaceContainerHighest,
                    content = colors.onSurfaceVariant,
                )
            }
            WaitingLabel("En revisión")
        }
        else -> {
            previousOf(task.column)?.let { prev ->
                TaskActionButton(
                    text = labelFor(prev),
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = { onMove(prev) },
                    container = colors.surfaceContainerHighest,
                    content = colors.onSurfaceVariant,
                )
            }
            nextOf(task.column)?.let { next ->
                val accent = columnColor(next)
                TaskActionButton(
                    text = if (next == TaskColumn.QA) "Enviar a QA" else labelFor(next),
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = { onMove(next) },
                    container = accent.copy(alpha = 0.16f),
                    content = accent,
                    iconFirst = false,
                )
            }
        }
    }
}

@Composable
private fun WaitingLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.HourglassTop,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(16.dp),
        )
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}