package com.svape.unal.ingesoftii.presentation.teams.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.Team
import com.svape.unal.ingesoftii.presentation.teams.NameDialog
import com.svape.unal.ingesoftii.presentation.teams.TeamMonogram
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import com.svape.unal.ingesoftii.ui.theme.pressScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTeamsScreen(
    courseId: String,
    viewModel: TeacherTeamsViewModel,
    onSelectTeam: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var teamToRename by remember { mutableStateOf<Team?>(null) }
    var teamToDelete by remember { mutableStateOf<Team?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    LaunchedEffect(courseId) {
        viewModel.loadTeams(courseId)
    }

    var lastSuccess by remember { mutableStateOf<TeacherTeamsUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TeacherTeamsUiState.Success -> lastSuccess = state
            is TeacherTeamsUiState.Error -> if (lastSuccess != null) snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }
    val content = (uiState as? TeacherTeamsUiState.Success) ?: lastSuccess

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Equipos",
                subtitle = content?.let {
                    val n = it.teams.size
                    if (n == 1) "Proyecto de aula · 1 equipo" else "Proyecto de aula · $n equipos"
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (content != null && content.teams.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Nuevo equipo") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    expanded = fabExpanded,
                    onClick = { showCreateDialog = true },
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
                content != null && content.teams.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.Groups,
                        title = "Aún no hay equipos",
                        message = "Crea los equipos del proyecto de aula y luego asigna a tus estudiantes en cada uno.",
                        action = {
                            Button(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Outlined.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Crear el primero")
                            }
                        },
                    )
                }

                content != null -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(content.teams, key = { _, team -> team.id }) { index, team ->
                        StaggeredEnter(index = index, modifier = Modifier.animateItem()) {
                            TeamCard(
                                team = team,
                                onClick = { onSelectTeam(team.id) },
                                onRename = { teamToRename = team },
                                onDelete = { teamToDelete = team },
                            )
                        }
                    }
                }

                state is TeacherTeamsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar los equipos",
                        message = state.message,
                        action = {
                            TextButton(onClick = { viewModel.loadTeams(courseId) }) { Text("Reintentar") }
                        },
                    )
                }

                else -> LoadingList(header = false, rows = 5, rowHeight = 80.dp)
            }
        }
    }

    if (showCreateDialog) {
        NameDialog(
            title = "Nuevo equipo",
            label = "Nombre del equipo",
            confirmLabel = "Crear",
            icon = Icons.Outlined.GroupAdd,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createTeam(name)
                showCreateDialog = false
                scope.launch { snackbarHostState.showSnackbar("Equipo \"$name\" creado") }
            },
        )
    }

    teamToRename?.let { team ->
        NameDialog(
            title = "Renombrar equipo",
            label = "Nombre del equipo",
            confirmLabel = "Guardar",
            icon = Icons.Outlined.DriveFileRenameOutline,
            initialValue = team.name,
            onDismiss = { teamToRename = null },
            onConfirm = { newName ->
                viewModel.renameTeam(team.id, newName)
                teamToRename = null
            },
        )
    }

    teamToDelete?.let { team ->
        val colors = MaterialTheme.colorScheme
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
            title = { Text("Eliminar equipo") },
            text = {
                Text("¿Eliminar \"${team.name}\"? Esto también borra todas sus tareas. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTeam(team.id)
                        teamToDelete = null
                        scope.launch { snackbarHostState.showSnackbar("Equipo eliminado") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { teamToDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val ext = IngesoftiiTheme.extendedColors
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    var showMenu by remember { mutableStateOf(false) }
    val members = team.memberUids.size

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction),
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamMonogram(team.name, size = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    team.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        tint = if (members == 0) colors.error else colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        when (members) {
                            0 -> "Sin integrantes"
                            1 -> "1 integrante"
                            else -> "$members integrantes"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (members == 0) colors.error else colors.onSurfaceVariant,
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones", tint = colors.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar equipo", color = colors.error) },
                        leadingIcon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = colors.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}