package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.presentation.grades.GradePill
import com.svape.unal.ingesoftii.presentation.grades.GradeRing
import com.svape.unal.ingesoftii.presentation.grades.MAX_GRADE
import com.svape.unal.ingesoftii.presentation.grades.StudentAvatar
import com.svape.unal.ingesoftii.presentation.grades.formatGrade
import com.svape.unal.ingesoftii.presentation.grades.gradeLevel
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ErrorState
import com.svape.unal.ingesoftii.ui.components.FullScreenImageViewer
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentGradeEditScreen(
    courseId: String,
    studentUid: String,
    viewModel: StudentGradeEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<GradeItem?>(null) }
    var zoomedPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseId, studentUid) { viewModel.load(courseId, studentUid) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    var lastSuccess by remember { mutableStateOf<StudentGradeEditUiState.Success?>(null) }
    LaunchedEffect(uiState) {
        (uiState as? StudentGradeEditUiState.Success)?.let {
            lastSuccess = it
        }
    }
    val content = (uiState as? StudentGradeEditUiState.Success) ?: lastSuccess

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Registrar notas",
                subtitle = content?.let { c ->
                    val graded = c.items.count { c.grades.containsKey(it.id) }
                    "$graded de ${c.items.size} calificados · escala 0.0 a 5.0"
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val state = uiState
        when {
            content != null -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content.student?.let { student ->
                    item(key = "student") {
                        StudentHeader(student, onPhotoClick = { zoomedPhoto = it })
                    }
                }
                if (content.items.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            icon = Icons.Outlined.School,
                            title = "Sin ítems de calificación",
                            message = "Crea ítems desde la pantalla de Calificaciones para poder registrar notas.",
                        )
                    }
                } else {
                    items(content.items, key = { it.id }) { item ->
                        GradeEditCard(
                            item = item,
                            current = content.grades[item.id],
                            onSave = { viewModel.saveGrade(item.id, it) },
                            onDelete = { pendingDelete = item },
                        )
                    }
                }
            }

            state is StudentGradeEditUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.load(courseId, studentUid) },
                modifier = Modifier.padding(padding),
            )

            else -> LoadingList(
                Modifier.padding(padding),
                header = false,
                rowHeight = 150.dp,
                rows = 4
            )
        }
    }

    zoomedPhoto?.let { url ->
        FullScreenImageViewer(imageUrl = url, onDismiss = { zoomedPhoto = null })
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text("¿Eliminar la nota?") },
            text = { Text("Se borrará la nota de \"${item.name}\". Podrás registrarla de nuevo después.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGrade(item.id)
                    pendingDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun StudentHeader(student: CourseStudent, onPhotoClick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val displayName = student.name.ifBlank { student.email }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StudentAvatar(displayName, student.photoUrl, size = 72.dp, onPhotoClick = onPhotoClick)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                displayName,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (student.email.isNotBlank() && student.email != displayName) {
                Spacer(Modifier.height(2.dp))
                Text(
                    student.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private val QuickGrades = listOf(3.0, 3.5, 4.0, 4.5, 5.0)

@Composable
private fun GradeEditCard(
    item: GradeItem,
    current: Double?,
    onSave: (Double) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val focusManager = LocalFocusManager.current

    var text by remember(item.id, current) { mutableStateOf(current?.let(::formatGrade) ?: "") }
    val parsed = text.replace(',', '.').toDoubleOrNull()
    val isValid = parsed != null && parsed in 0.0..MAX_GRADE
    val isChanged = parsed != current
    val showError = text.isNotBlank() && !isValid

    val save = {
        if (isValid && isChanged) {
            focusManager.clearFocus()
            onSave(parsed!!)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradeRing(
                    grade = current,
                    size = 48.dp,
                    strokeWidth = 5.dp,
                    textStyle = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    GradePill(gradeLevel(current))
                }
                if (current != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar nota",
                            tint = colors.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input.filter { it.isDigit() || it == '.' || it == ',' }.take(4)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nota") },
                    placeholder = { Text("0.0 – 5.0") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Debe estar entre 0.0 y 5.0") }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        focusedContainerColor = colors.surfaceContainerLow,
                        errorContainerColor = colors.surfaceContainerLow,
                    ),
                )
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { save() },
                    enabled = isValid && isChanged,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .heightIn(min = 52.dp),
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar")
                }
            }

            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickGrades.forEach { value ->
                    val label = formatGrade(value)
                    FilterChip(
                        selected = text == label,
                        onClick = { text = label },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}