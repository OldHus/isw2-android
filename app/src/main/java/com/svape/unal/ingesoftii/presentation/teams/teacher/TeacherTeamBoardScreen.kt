package com.svape.unal.ingesoftii.presentation.teams.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.presentation.grades.StudentAvatar
import com.svape.unal.ingesoftii.presentation.grades.segmentedShape
import com.svape.unal.ingesoftii.presentation.teams.BoardHeaderCard
import com.svape.unal.ingesoftii.presentation.teams.BoardProgress
import com.svape.unal.ingesoftii.presentation.teams.KanbanBoard
import com.svape.unal.ingesoftii.presentation.teams.MemberAvatarStack
import com.svape.unal.ingesoftii.presentation.teams.TaskActionButton
import com.svape.unal.ingesoftii.presentation.teams.TaskFormDialog
import com.svape.unal.ingesoftii.presentation.teams.TeamMonogram
import com.svape.unal.ingesoftii.presentation.teams.columnColor
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTeamBoardScreen(
    courseId: String,
    teamId: String,
    viewModel: TeacherTeamBoardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMembers by rememberSaveable { mutableStateOf(false) }
    var showCreateTaskDialog by rememberSaveable { mutableStateOf(false) }
    var taskToReject by remember { mutableStateOf<KanbanTask?>(null) }
    var memberToRemove by remember { mutableStateOf<CourseStudent?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(courseId, teamId) {
        viewModel.load(courseId, teamId)
    }

    var lastSuccess by remember { mutableStateOf<TeacherTeamBoardUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeacherTeamBoardUiState.Success -> lastSuccess = state
            is TeacherTeamBoardUiState.Error -> if (lastSuccess != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val content = (uiState as? TeacherTeamBoardUiState.Success) ?: lastSuccess

    fun notify(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = content?.team?.name ?: "Equipo",
                subtitle = content?.let { "${it.members.size} integrantes · ${it.tasks.size} tareas" },
                actions = {
                    if (content != null) {
                        IconButton(onClick = { showMembers = true }) {
                            Icon(Icons.Outlined.ManageAccounts, contentDescription = "Gestionar integrantes")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (content != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Nueva tarea") },
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
                                if (content.members.isEmpty()) {
                                    Text(
                                        "Sin integrantes todavía",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Spacer(Modifier.height(6.dp))
                                    MemberAvatarStack(content.members, size = 30.dp)
                                }
                            }
                            FilledTonalButton(onClick = { showMembers = true }, shape = CircleShape) {
                                Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("Integrantes")
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        BoardProgress(content.tasks)
                    }

                    val initialColumn = when {
                        content.tasks.any { it.column == TaskColumn.PROPUESTAS } -> TaskColumn.PROPUESTAS
                        content.tasks.any { it.column == TaskColumn.QA } -> TaskColumn.QA
                        else -> TaskColumn.HACER
                    }
                    KanbanBoard(tasks = content.tasks, initialColumn = initialColumn) { task ->
                        TeacherTaskActions(
                            task = task,
                            onApproveProposal = {
                                viewModel.moveTask(task.id, TaskColumn.PROPUESTAS, TaskColumn.HACER)
                                notify("Propuesta aprobada: pasa a Hacer")
                            },
                            onRejectProposal = { taskToReject = task },
                            onApproveQa = {
                                viewModel.moveTask(task.id, TaskColumn.QA, TaskColumn.APROBADA)
                                notify("¡Tarea aprobada! 🎉")
                            },
                            onSendBackToDoing = {
                                viewModel.moveTask(task.id, TaskColumn.QA, TaskColumn.DESARROLLANDO)
                                notify("Tarea devuelta a Desarrollando")
                            },
                        )
                    }
                }

                state is TeacherTeamBoardUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Outlined.CloudOff, title = "Algo salió mal", message = state.message)
                }

                else -> LoadingList(header = true, rows = 4, rowHeight = 96.dp)
            }
        }
    }

    if (showCreateTaskDialog) {
        TaskFormDialog(
            title = "Nueva tarea",
            confirmLabel = "Crear",
            icon = Icons.Outlined.AddTask,
            onDismiss = { showCreateTaskDialog = false },
            onConfirm = { title, description ->
                viewModel.createTask(title, description)
                showCreateTaskDialog = false
                notify("Tarea creada")
            },
        )
    }

    taskToReject?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToReject = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text("¿Rechazar la propuesta?") },
            text = { Text("\"${task.title}\" se eliminará del tablero del equipo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task.id)
                    taskToReject = null
                    notify("Propuesta rechazada")
                }) { Text("Rechazar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { taskToReject = null }) { Text("Cancelar") } },
        )
    }

    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            icon = { Icon(Icons.Outlined.PersonRemove, contentDescription = null) },
            title = { Text("¿Quitar del equipo?") },
            text = { Text("${member.name.ifBlank { member.email }} dejará de ver el tablero de este equipo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeStudent(member)
                    memberToRemove = null
                    notify("Integrante retirado")
                }) { Text("Quitar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { memberToRemove = null }) { Text("Cancelar") } },
        )
    }

    if (showMembers && content != null) {
        MembersSheet(
            members = content.members,
            availableStudents = content.availableStudents,
            onDismiss = { showMembers = false },
            onAdd = { student ->
                viewModel.addStudent(student)
                notify("${student.name.ifBlank { student.email }} agregado al equipo")
            },
            onRemove = { memberToRemove = it },
        )
    }
}

@Composable
private fun TeacherTaskActions(
    task: KanbanTask,
    onApproveProposal: () -> Unit,
    onRejectProposal: () -> Unit,
    onApproveQa: () -> Unit,
    onSendBackToDoing: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    when (task.column) {
        TaskColumn.PROPUESTAS -> {
            TaskActionButton(
                text = "Rechazar",
                icon = Icons.Rounded.Close,
                onClick = onRejectProposal,
                container = colors.errorContainer,
                content = colors.onErrorContainer,
            )
            TaskActionButton(
                text = "Aprobar",
                icon = Icons.Rounded.Check,
                onClick = onApproveProposal,
                container = columnColor(TaskColumn.HACER).copy(alpha = 0.16f),
                content = columnColor(TaskColumn.HACER),
            )
        }

        TaskColumn.QA -> {
            TaskActionButton(
                text = "Devolver",
                icon = Icons.AutoMirrored.Rounded.Undo,
                onClick = onSendBackToDoing,
                container = colors.surfaceContainerHighest,
                content = colors.onSurfaceVariant,
            )
            TaskActionButton(
                text = "Aprobar",
                icon = Icons.Rounded.DoneAll,
                onClick = onApproveQa,
                container = ext.successContainer,
                content = ext.onSuccessContainer,
            )
        }

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembersSheet(
    members: List<CourseStudent>,
    availableStudents: List<CourseStudent>,
    onDismiss: () -> Unit,
    onAdd: (CourseStudent) -> Unit,
    onRemove: (CourseStudent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") }
    val filteredAvailable = remember(availableStudents, query) {
        val q = query.trim()
        if (q.isEmpty()) availableStudents
        else availableStudents.filter { it.name.contains(q, true) || it.email.contains(q, true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        ) {
            item {
                Text("Integrantes del equipo", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Toca − para quitar a alguien o + para agregarlo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                SectionHeader("En el equipo (${members.size})", Modifier.padding(top = 16.dp))
            }
            if (members.isEmpty()) {
                item {
                    Text(
                        "Todavía no hay integrantes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                itemsIndexed(members, key = { _, m -> "m_" + m.uid }) { index, member ->
                    PersonRow(
                        student = member,
                        index = index,
                        count = members.size,
                        actionIcon = Icons.Outlined.PersonRemove,
                        actionDescription = "Quitar del equipo",
                        actionTint = colors.error,
                        onAction = { onRemove(member) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            item {
                SectionHeader("Agregar estudiantes (${availableStudents.size})", Modifier.padding(top = 20.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("Buscar por nombre o correo") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.surfaceContainer,
                        focusedContainerColor = colors.surfaceContainer,
                    ),
                )
            }
            if (filteredAvailable.isEmpty()) {
                item {
                    Text(
                        if (availableStudents.isEmpty()) "No hay más estudiantes disponibles para agregar."
                        else "Sin resultados para \"$query\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                itemsIndexed(filteredAvailable, key = { _, s -> "a_" + s.uid }) { index, student ->
                    PersonRow(
                        student = student,
                        index = index,
                        count = filteredAvailable.size,
                        actionIcon = Icons.Outlined.GroupAdd,
                        actionDescription = "Agregar al equipo",
                        actionTint = colors.primary,
                        onAction = { onAdd(student) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonRow(
    student: CourseStudent,
    index: Int,
    count: Int,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    actionDescription: String,
    actionTint: androidx.compose.ui.graphics.Color,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val displayName = student.name.ifBlank { student.email }
    Surface(
        shape = segmentedShape(index, count),
        color = IngesoftiiTheme.extendedColors.card,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp),
    ) {
        Row(
            Modifier
                .heightIn(min = 60.dp)
                .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            StudentAvatar(displayName, student.photoUrl, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (student.email.isNotBlank() && student.email != displayName) {
                    Text(
                        student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onAction) {
                Icon(actionIcon, contentDescription = actionDescription, tint = actionTint)
            }
        }
    }
}