package com.svape.unal.ingesoftii.presentation.attendance.teacher

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.presentation.attendance.CountdownRing
import com.svape.unal.ingesoftii.presentation.attendance.rememberRemainingSeconds
import com.svape.unal.ingesoftii.presentation.grades.StudentAvatar
import com.svape.unal.ingesoftii.presentation.grades.segmentedShape
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.IconBubble
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.PrimaryButton
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.SpaceGrotesk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DurationOptions = listOf(2, 5, 10, 15)

private const val CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
private const val MAX_CODE_LENGTH = 12
private const val URGENT_THRESHOLD_SECONDS = 15

private val ProjectorNavy = Color(0xFF011252)
private val ProjectorBlue = Color(0xFF0A2A7A)
private val ProjectorAccent = Color(0xFF4CC9F0)
private val ProjectorUrgent = Color(0xFFFF8A80)

private fun generateCode(length: Int = 4): String =
    (1..length).map { CODE_ALPHABET.random() }.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    courseId: String,
    viewModel: TeacherAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(courseId) {
        viewModel.start(courseId)
    }

    val share: () -> Unit = {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, viewModel.buildExportText())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir asistencia"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Asistencia",
                subtitle = (uiState as? TeacherAttendanceUiState.Active)?.let { "Sesión activa · en vivo" },
                actions = {
                    if (uiState is TeacherAttendanceUiState.Active) {
                        IconButton(onClick = share) {
                            Icon(Icons.Rounded.Share, contentDescription = "Compartir asistencia")
                        }
                    }
                },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            contentKey = { it::class },
            transitionSpec = {
                (fadeIn(tween(Motion.Long)) +
                        scaleIn(
                            tween(Motion.Long, easing = Motion.EmphasizedDecelerate),
                            initialScale = 0.96f
                        )) togetherWith fadeOut(tween(Motion.Short))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "teacherAttendance",
        ) { state ->
            when (state) {
                TeacherAttendanceUiState.Loading -> LoadingList(header = true, rows = 3)

                is TeacherAttendanceUiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Algo salió mal",
                        message = state.message,
                    )
                }

                TeacherAttendanceUiState.NoActiveSession -> StartSessionForm(
                    isStarting = isStarting,
                    onStart = { code, duration -> viewModel.startSession(code, duration) },
                )

                is TeacherAttendanceUiState.Active -> ActiveSessionView(
                    state = state,
                    onExpired = { viewModel.closeActiveSession() },
                    onCloseManually = { viewModel.closeActiveSession() },
                    onShare = share,
                )
            }
        }
    }
}

@Composable
private fun StartSessionForm(
    isStarting: Boolean,
    onStart: (String, Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var code by rememberSaveable { mutableStateOf(generateCode()) }
    var duration by rememberSaveable { mutableIntStateOf(5) }
    var customDuration by rememberSaveable { mutableStateOf("") }

    val effectiveDuration = customDuration.toIntOrNull()?.takeIf { it > 0 } ?: duration
    val canStart = code.isNotBlank() && effectiveDuration > 0

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 520.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(Icons.Outlined.GroupAdd, size = 52.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Tomar asistencia", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Comparte el código en clase; los estudiantes lo ingresan desde su celular.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Código")

            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    code = input.uppercase().filterNot { it.isWhitespace() }.take(MAX_CODE_LENGTH)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { code = generateCode() }) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = "Generar otro código")
                    }
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = SpaceGrotesk,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center,
                    color = colors.onSurface,
                ),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedBorderColor = colors.primary,
                    unfocusedContainerColor = colors.surfaceContainerLow,
                    focusedContainerColor = colors.surfaceContainerLow,
                ),
            )
            Text(
                "Toca ✨ para generar uno nuevo. Evitamos letras confusas como O/0 o I/1.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )

            Spacer(Modifier.height(20.dp))
            SectionHeader("Duración")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DurationOptions.forEach { minutes ->
                    FilterChip(
                        selected = customDuration.isBlank() && duration == minutes,
                        onClick = {
                            duration = minutes
                            customDuration = ""
                            focusManager.clearFocus()
                        },
                        label = { Text("$minutes min") },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = customDuration,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 3) customDuration = input
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Otra duración (minutos)") },
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedBorderColor = colors.primary,
                    unfocusedContainerColor = colors.surfaceContainerLow,
                    focusedContainerColor = colors.surfaceContainerLow,
                ),
            )

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = "Activar código · $effectiveDuration min",
                onClick = {
                    focusManager.clearFocus()
                    onStart(code.trim(), effectiveDuration)
                },
                enabled = canStart,
                loading = isStarting,
                leadingIcon = Icons.Rounded.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActiveSessionView(
    state: TeacherAttendanceUiState.Active,
    onExpired: () -> Unit,
    onCloseManually: () -> Unit,
    onShare: () -> Unit,
) {
    val session = state.session
    val remaining = rememberRemainingSeconds(session.expiresAt)
    val total = remember(session.id) { remaining.coerceAtLeast(1) }
    var confirmClose by remember { mutableStateOf(false) }
    var projector by rememberSaveable { mutableStateOf(false) }

    val currentOnExpired by rememberUpdatedState(onExpired)
    LaunchedEffect(session.id, remaining == 0L) {
        if (remaining == 0L) currentOnExpired()
    }

    val entries = remember(state.entries) { state.entries.reversed() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
    ) {
        item(key = "hero") {
            CodeHeroCard(
                session = session,
                remaining = remaining,
                total = total,
                onProjector = { projector = true },
            )
        }

        item(key = "actions") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = { confirmClose = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        Icons.Outlined.StopCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar ahora", color = MaterialTheme.colorScheme.error)
                }
                FilledTonalButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = CircleShape,
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir")
                }
            }
        }

        item(key = "progress") {
            AttendanceProgress(
                registered = state.entries.size,
                total = state.totalStudents,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            )
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Outlined.GroupAdd,
                    title = "Aún nadie se registra",
                    message = "Los estudiantes aparecerán aquí en tiempo real a medida que ingresen el código.",
                )
            }
        } else {
            itemsIndexed(
                entries,
                key = { _, e -> e.studentEmail + e.registeredAtMillis }
            ) { index, entry ->
                EntryRow(
                    entry = entry,
                    position = entries.size - index,
                    shapeIndex = index,
                    count = entries.size,
                    modifier = Modifier
                        .animateItem()
                        .padding(bottom = 3.dp),
                )
            }
        }
    }

    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            icon = { Icon(Icons.Outlined.StopCircle, contentDescription = null) },
            title = { Text("¿Cerrar la sesión ahora?") },
            text = {
                Text("Los estudiantes que aún no se registren ya no podrán hacerlo. Llevas ${state.entries.size} registrado(s).")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmClose = false
                    onCloseManually()
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClose = false }) { Text("Seguir abierta") }
            },
        )
    }

    if (projector) {
        ProjectorDialog(
            session = session,
            remaining = remaining,
            total = total,
            registered = state.entries.size,
            totalStudents = state.totalStudents,
            onDismiss = { projector = false },
        )
    }
}

@Composable
private fun CodeHeroCard(
    session: AttendanceSession,
    remaining: Long,
    total: Long,
    onProjector: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.primary,
        contentColor = colors.onPrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    listOf(colors.primary, lerp(colors.primary, colors.onPrimaryContainer, 0.45f))
                ),
            ),
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Código activo", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        session.code,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = SpaceGrotesk,
                            letterSpacing = 4.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        onClick = onProjector,
                        shape = CircleShape,
                        color = colors.onPrimary.copy(alpha = 0.16f),
                        contentColor = colors.onPrimary,
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Fullscreen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Proyectar", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                CountdownRing(
                    remainingSeconds = remaining,
                    totalSeconds = total,
                    size = 104.dp,
                    strokeWidth = 8.dp,
                    color = if (remaining <= URGENT_THRESHOLD_SECONDS) colors.errorContainer else colors.onPrimary,
                    trackColor = colors.onPrimary.copy(alpha = 0.22f),
                    textColor = colors.onPrimary,
                    timeStyle = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
private fun AttendanceProgress(registered: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val fraction = if (total <= 0) 0f else (registered.toFloat() / total).coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        fraction,
        tween(700, easing = Motion.EmphasizedDecelerate),
        label = "attendanceProgress"
    )

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "$registered",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (total > 0) "de $total estudiantes registrados" else "registrado(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            if (total > 0) {
                Text(
                    "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = ext.success
                )
            }
        }
        if (total > 0) {
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
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun EntryRow(
    entry: AttendanceEntry,
    position: Int,
    shapeIndex: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val time = remember(entry.registeredAtMillis) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.registeredAtMillis))
    }
    Surface(
        shape = segmentedShape(shapeIndex, count),
        color = ext.card,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "#$position",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.width(34.dp),
            )
            StudentAvatar(entry.studentName, entry.photoUrl, size = 38.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.studentName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.studentEmail.isNotBlank()) {
                    Text(
                        entry.studentEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = ext.successContainer,
                contentColor = ext.onSuccessContainer,
            ) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ProjectorDialog(
    session: AttendanceSession,
    remaining: Long,
    total: Long,
    registered: Int,
    totalStudents: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ProjectorBlue, ProjectorNavy))),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Código de asistencia",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    session.code,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = SpaceGrotesk,
                        fontSize = 88.sp,
                        lineHeight = 96.sp,
                        letterSpacing = 10.sp,
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(28.dp))
                CountdownRing(
                    remainingSeconds = remaining,
                    totalSeconds = total,
                    size = 160.dp,
                    strokeWidth = 12.dp,
                    color = if (remaining <= URGENT_THRESHOLD_SECONDS) ProjectorUrgent else ProjectorAccent,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    textColor = Color.White,
                    timeStyle = MaterialTheme.typography.displaySmall,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    if (totalStudents > 0) "$registered de $totalStudents registrados" else "$registered registrados",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            FilledTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .safeDrawingPadding()
                    .padding(12.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.16f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Salir del modo proyector")
            }
        }
    }
}