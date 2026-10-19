package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.domain.model.GradeItemType
import com.svape.unal.ingesoftii.presentation.grades.StudentAvatar
import com.svape.unal.ingesoftii.presentation.grades.segmentedShape
import com.svape.unal.ingesoftii.ui.components.AppTextField
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ErrorBanner
import com.svape.unal.ingesoftii.ui.components.ErrorState
import com.svape.unal.ingesoftii.ui.components.FullScreenImageViewer
import com.svape.unal.ingesoftii.ui.components.IconBubble
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradesScreen(
    courseId: String,
    viewModel: TeacherGradesViewModel,
    onGradeStudent: (String) -> Unit,
    onViewMatrix: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddItemDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var zoomedPhoto by remember { mutableStateOf<String?>(null) }
    var actionsFor by remember { mutableStateOf<GradeItem?>(null) }
    var renaming by remember { mutableStateOf<GradeItem?>(null) }
    var deleting by remember { mutableStateOf<GradeItem?>(null) }

    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(courseId) { viewModel.load(courseId) }

    var lastSuccess by remember { mutableStateOf<TeacherGradesUiState.Success?>(null) }
    LaunchedEffect(uiState) { (uiState as? TeacherGradesUiState.Success)?.let { lastSuccess = it } }
    val content = (uiState as? TeacherGradesUiState.Success) ?: lastSuccess

    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Calificaciones",
                subtitle = content?.let { "${it.students.size} estudiantes · ${it.items.size} ítems" },
                actions = {
                    IconButton(onClick = onViewMatrix) {
                        Icon(Icons.Outlined.TableChart, contentDescription = "Ver matriz de notas")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo ítem") },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                onClick = { showAddItemDialog = true },
                expanded = fabExpanded,
            )
        },
    ) { padding ->
        val state = uiState
        when {
            content != null -> PullToRefreshBox(
                isRefreshing = state is TeacherGradesUiState.Loading,
                onRefresh = { viewModel.load(courseId) },
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                val filtered = remember(content.students, query) {
                    val q = query.trim()
                    if (q.isEmpty()) content.students
                    else content.students.filter {
                        it.name.contains(q, ignoreCase = true) || it.email.contains(q, ignoreCase = true)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
                ) {
                    (state as? TeacherGradesUiState.Error)?.let { error ->
                        item { ErrorBanner(error.message, Modifier.padding(bottom = 12.dp)) }
                    }

                    item(key = "itemsHeader") { SectionHeader("Ítems de calificación") }
                    item(key = "items") {
                        GradeItemsRow(
                            items = content.items,
                            onAdd = { showAddItemDialog = true },
                            onItemClick = { actionsFor = it },
                        )
                    }

                    item(key = "matrix") {
                        MatrixShortcut(onViewMatrix, Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }

                    item(key = "studentsHeader") {
                        SectionHeader("Estudiantes (${content.students.size})", Modifier.padding(top = 8.dp))
                    }
                    item(key = "search") {
                        SearchField(query, onQueryChange = { query = it }, Modifier.padding(bottom = 12.dp))
                    }

                    if (filtered.isEmpty()) {
                        item(key = "noResults") {
                            EmptyState(
                                icon = Icons.Outlined.PersonSearch,
                                title = if (query.isBlank()) "Sin estudiantes" else "Sin resultados",
                                message = if (query.isBlank()) "Aún no hay estudiantes inscritos en el curso."
                                else "No encontramos estudiantes que coincidan con \"$query\".",
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, s -> s.uid }) { index, student ->
                            StudentRow(
                                student = student,
                                index = index,
                                count = filtered.size,
                                onClick = { onGradeStudent(student.uid) },
                                onPhotoClick = { zoomedPhoto = it },
                                modifier = Modifier
                                    .animateItem()
                                    .padding(bottom = 3.dp),
                            )
                        }
                    }
                }
            }

            state is TeacherGradesUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.load(courseId) },
                modifier = Modifier.padding(padding),
            )

            else -> LoadingList(Modifier.padding(padding), header = false)
        }
    }

    zoomedPhoto?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { zoomedPhoto = null })
    }

    actionsFor?.let { item ->
        GradeItemActionsSheet(
            item = item,
            onDismiss = { actionsFor = null },
            onRename = {
                actionsFor = null
                renaming = item
            },
            onDelete = {
                actionsFor = null
                deleting = item
            },
        )
    }

    renaming?.let { item ->
        RenameGradeItemDialog(
            currentName = item.name,
            onDismiss = { renaming = null },
            onConfirm = { newName ->
                viewModel.renameItem(item.id, newName)
                renaming = null
            },
        )
    }

    deleting?.let { item ->
        DeleteGradeItemDialog(
            item = item,
            countStudentsGraded = { viewModel.countStudentsGraded(item.id) },
            onDismiss = { deleting = null },
            onConfirm = {
                viewModel.deleteItem(item.id)
                deleting = null
            },
        )
    }

    if (showAddItemDialog) {
        AddGradeItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, weight, type ->
                viewModel.addItem(name, weight, type)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
private fun GradeItemsRow(items: List<GradeItem>, onAdd: () -> Unit, onItemClick: (GradeItem) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    if (items.isEmpty()) {
        Surface(
            onClick = onAdd,
            shape = MaterialTheme.shapes.large,
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.AutoMirrored.Rounded.PlaylistAdd)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Crea el primer ítem", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Por ejemplo: Parcial 1, Taller, Quiz semanal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
            Surface(
                onClick = { onItemClick(item) },
                shape = MaterialTheme.shapes.large,
                color = ext.card,
                border = BorderStroke(1.dp, ext.cardBorder),
                modifier = Modifier
                    .animateItem()
                    .widthIn(min = 132.dp, max = 180.dp),
            ) {
                Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = colors.primaryContainer, contentColor = colors.onPrimaryContainer) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Opciones de ${item.name}",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixShortcut(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Outlined.TableChart, container = colors.primary, content = colors.onPrimary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Matriz de notas", style = MaterialTheme.typography.titleMedium)
                Text("Todas las notas del curso en una sola tabla", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por nombre o correo") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Limpiar búsqueda")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = colors.outlineVariant,
            focusedBorderColor = colors.primary,
            unfocusedContainerColor = colors.surfaceContainerLow,
            focusedContainerColor = colors.surfaceContainerLow,
        ),
    )
}

@Composable
private fun StudentRow(
    student: CourseStudent,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val displayName = student.name.ifBlank { student.email }
    Surface(
        onClick = onClick,
        shape = segmentedShape(index, count),
        color = IngesoftiiTheme.extendedColors.card,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .heightIn(min = 68.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StudentAvatar(displayName, student.photoUrl, onPhotoClick = onPhotoClick)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(displayName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    student.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun AddGradeItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, weight: Double, type: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var isDynamic by rememberSaveable { mutableStateOf(true) }
    val colors = MaterialTheme.colorScheme
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = null) },
        title = { Text("Nuevo ítem de calificación") },
        text = {
            Column {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    placeholder = "Ej. Parcial 1",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    onClick = { isDynamic = !isDynamic },
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surfaceContainerHigh,
                ) {
                    Row(
                        Modifier.padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Quiz o tarea", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Tipo dinámico: se califica varias veces en el semestre",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = isDynamic, onCheckedChange = { isDynamic = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val type = if (isDynamic) GradeItemType.DYNAMIC else GradeItemType.FIXED
                    onConfirm(name.trim(), 0.0, type)
                },
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradeItemActionsSheet(
    item: GradeItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 24.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            SheetAction(Icons.Outlined.Edit, "Cambiar nombre", colors.onSurface, onRename)
            SheetAction(Icons.Outlined.DeleteOutline, "Eliminar ítem", colors.error, onDelete)
        }
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = tint)
        }
    }
}

@Composable
private fun RenameGradeItemDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(currentName) }
    val trimmed = name.trim()
    val canSave = trimmed.isNotEmpty() && trimmed != currentName

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        title = { Text("Cambiar nombre") },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre del ítem",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(trimmed) }, enabled = canSave) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun DeleteGradeItemDialog(
    item: GradeItem,
    countStudentsGraded: suspend () -> Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var affected by remember(item.id) { mutableStateOf<Int?>(null) }
    LaunchedEffect(item.id) { affected = countStudentsGraded() }
    val count = affected

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (count != null && count > 0) Icons.Outlined.WarningAmber else Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = if (count != null && count > 0) colors.error else colors.onSurfaceVariant,
            )
        },
        title = { Text("¿Eliminar \"${item.name}\"?") },
        text = {
            when {
                count == null -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Revisando si hay notas registradas…")
                }
                count == 0 -> Text("Ningún estudiante tiene nota en este ítem.")
                count == 1 -> Text("1 estudiante ya tiene nota en este ítem. También se eliminará esa nota.")
                else -> Text("$count estudiantes ya tienen nota en este ítem. También se eliminarán esas notas.")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = count != null) {
                Text("Eliminar", color = if (count != null) colors.error else colors.onSurfaceVariant)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}