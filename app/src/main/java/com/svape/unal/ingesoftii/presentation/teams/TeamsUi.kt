package com.svape.unal.ingesoftii.presentation.teams

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.ui.components.AppTextField
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.LocalIsDarkTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import kotlinx.coroutines.launch


data class BoardColumn(
    val column: TaskColumn,
    val label: String,
    val emptyTitle: String,
    val emptyMessage: String,
    val icon: ImageVector,
)

val BoardColumns = listOf(
    BoardColumn(
        TaskColumn.PROPUESTAS, "Propuestas", "Sin propuestas",
        "Las ideas nuevas del equipo esperan aquí la aprobación del docente.", Icons.Outlined.Lightbulb,
    ),
    BoardColumn(
        TaskColumn.HACER, "Hacer", "Nada pendiente",
        "Las tareas aprobadas aparecen aquí listas para empezar.", Icons.Outlined.Checklist,
    ),
    BoardColumn(
        TaskColumn.DESARROLLANDO, "Desarrollando", "Nada en desarrollo",
        "Mueve aquí las tareas en las que el equipo está trabajando.", Icons.Outlined.Code,
    ),
    BoardColumn(
        TaskColumn.QA, "QA", "Nada en revisión",
        "Cuando una tarea esté lista, pásala a QA para que el docente la revise.", Icons.Outlined.FactCheck,
    ),
    BoardColumn(
        TaskColumn.APROBADA, "Aprobada", "Aún sin tareas aprobadas",
        "Las tareas validadas por el docente se celebran aquí. 🎉", Icons.Outlined.Verified,
    ),
)

fun labelFor(column: TaskColumn): String = BoardColumns.first { it.column == column }.label


@Composable
fun columnColor(column: TaskColumn): Color {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val isDark = LocalIsDarkTheme.current
    return when (column) {
        TaskColumn.PROPUESTAS -> colors.secondary
        TaskColumn.HACER -> colors.primary
        TaskColumn.DESARROLLANDO -> colors.tertiary
        TaskColumn.QA -> if (isDark) Color(0xFFB9A4FF) else Color(0xFF6B4FD8)
        TaskColumn.APROBADA -> ext.success
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KanbanBoard(
    tasks: List<KanbanTask>,
    modifier: Modifier = Modifier,
    initialColumn: TaskColumn = TaskColumn.HACER,
    cardActions: @Composable RowScope.(KanbanTask) -> Unit,
) {
    val isWide = LocalConfiguration.current.screenWidthDp >= 840
    if (isWide) {
        LazyRow(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(BoardColumns, key = { it.column.name }) { spec ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight(),
                ) {
                    Column {
                        ColumnHeader(spec, tasks.count { it.column == spec.column }, Modifier.padding(16.dp))
                        ColumnTasks(spec, tasks, cardActions)
                    }
                }
            }
        }
        return
    }

    val initialPage = BoardColumns.indexOfFirst { it.column == initialColumn }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { BoardColumns.size }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            divider = {},
        ) {
            BoardColumns.forEachIndexed { index, spec ->
                val count = tasks.count { it.column == spec.column }
                val selected = pagerState.currentPage == index
                val tint = if (selected) columnColor(spec.column) else MaterialTheme.colorScheme.onSurfaceVariant
                Tab(
                    selected = selected,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(spec.icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(spec.label, color = tint, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(6.dp))
                            CountBadge(count, columnColor(spec.column), highlighted = selected)
                        }
                    },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp),
            beyondViewportPageCount = 1,
        ) { page ->
            ColumnTasks(BoardColumns[page], tasks, cardActions)
        }
    }
}

@Composable
private fun ColumnHeader(spec: BoardColumn, count: Int, modifier: Modifier = Modifier) {
    val color = columnColor(spec.column)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(spec.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(spec.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        CountBadge(count, color, highlighted = true)
    }
}

@Composable
private fun ColumnTasks(
    spec: BoardColumn,
    tasks: List<KanbanTask>,
    cardActions: @Composable RowScope.(KanbanTask) -> Unit,
) {
    val columnTasks = remember(tasks, spec.column) {
        tasks.filter { it.column == spec.column }
            .sortedWith(compareBy<KanbanTask>({ it.order }, { it.createdAt }))
    }
    val accent = columnColor(spec.column)
    if (columnTasks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            EmptyState(icon = spec.icon, title = spec.emptyTitle, message = spec.emptyMessage)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(columnTasks, key = { it.id }) { task ->
            TaskCard(task = task, accent = accent, modifier = Modifier.animateItem()) {
                cardActions(task)
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, color: Color, highlighted: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (highlighted) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (highlighted) color else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            "$count",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun TaskCard(
    task: KanbanTask,
    accent: Color,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val ext = IngesoftiiTheme.extendedColors
    var expanded by rememberSaveable(task.id) { mutableStateOf(false) }
    Surface(
        onClick = { expanded = !expanded },
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                Modifier
                    .weight(1f)
                    .animateContentSize(tween(Motion.Medium))
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            ) {
                Text(task.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
fun TaskActionButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    iconFirst: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = container, contentColor = content),
        modifier = Modifier.heightIn(min = 36.dp),
    ) {
        if (icon != null && iconFirst) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
        if (icon != null && !iconFirst) {
            Spacer(Modifier.width(6.dp))
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}


@Composable
fun BoardProgress(tasks: List<KanbanTask>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val committed = tasks.count { it.column != TaskColumn.PROPUESTAS }
    val approved = tasks.count { it.column == TaskColumn.APROBADA }
    val fraction = if (committed == 0) 0f else approved.toFloat() / committed
    val animated by animateFloatAsState(fraction, tween(900, easing = Motion.EmphasizedDecelerate), label = "boardProgress")

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Avance del proyecto", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                "$approved de $committed aprobadas",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = ext.success,
            trackColor = colors.surfaceContainerHighest,
            drawStopIndicator = {},
        )
    }
}


@Composable
fun MemberAvatarStack(
    members: List<CourseStudent>,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    max: Int = 5,
) {
    val colors = MaterialTheme.colorScheme
    val visible = members.take(max)
    val extra = members.size - visible.size
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        visible.forEachIndexed { index, member ->
            ProfileAvatar(
                name = member.name.ifBlank { member.email },
                photoUrl = member.photoUrl,
                role = UserRole.STUDENT,
                size = size,
                modifier = Modifier
                    .offset(x = (-10).dp * index)
                    .border(2.dp, colors.surface, CircleShape),
            )
        }
        if (extra > 0) {
            Box(
                Modifier
                    .offset(x = (-10).dp * visible.size)
                    .size(size)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHighest)
                    .border(2.dp, colors.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$extra", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}


@Composable
fun TaskFormDialog(
    title: String,
    confirmLabel: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String) -> Unit,
) {
    var taskTitle by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(title) },
        text = {
            Column {
                AppTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it.take(80) },
                    label = "Título",
                    placeholder = "Ej. Diseñar pantalla de login",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = description,
                    onValueChange = { description = it.take(500) },
                    label = "Descripción (opcional)",
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(taskTitle.trim(), description.trim()) },
                enabled = taskTitle.isNotBlank(),
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
fun NameDialog(
    title: String,
    label: String,
    confirmLabel: String,
    icon: ImageVector,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialValue) }
    val trimmed = name.trim()
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(title) },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it.take(60) },
                label = label,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed) },
                enabled = trimmed.isNotEmpty() && trimmed != initialValue.trim(),
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}


@Composable
fun teamColors(name: String): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val palette = listOf(
        colors.primaryContainer to colors.onPrimaryContainer,
        colors.tertiaryContainer to colors.onTertiaryContainer,
        colors.secondaryContainer to colors.onSecondaryContainer,
        ext.successContainer to ext.onSuccessContainer,
    )
    return palette[(name.hashCode() and Int.MAX_VALUE) % palette.size]
}


@Composable
fun TeamMonogram(name: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val (bg, fg) = teamColors(name)
    Box(
        modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleLarge,
            color = fg,
        )
    }
}


@Composable
fun BoardHeaderCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = IngesoftiiTheme.extendedColors.card,
        border = BorderStroke(1.dp, IngesoftiiTheme.extendedColors.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}