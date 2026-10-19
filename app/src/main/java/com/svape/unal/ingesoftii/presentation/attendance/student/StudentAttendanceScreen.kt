package com.svape.unal.ingesoftii.presentation.attendance.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.domain.model.AttendanceSession
import com.svape.unal.ingesoftii.presentation.attendance.CountdownRing
import com.svape.unal.ingesoftii.presentation.attendance.rememberRemainingSeconds
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ErrorBanner
import com.svape.unal.ingesoftii.ui.components.PrimaryButton
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    courseId: String,
    viewModel: StudentAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.start(courseId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "Asistencia") },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = uiState,
                contentKey = { it::class },
                transitionSpec = {
                    (fadeIn(tween(Motion.Long)) +
                            scaleIn(
                                tween(Motion.Long, easing = Motion.EmphasizedDecelerate),
                                initialScale = 0.94f
                            )) togetherWith fadeOut(tween(Motion.Short))
                },
                label = "attendanceState",
            ) { state ->
                when (state) {
                    StudentAttendanceUiState.Loading -> WaitingView(loading = true)

                    is StudentAttendanceUiState.Error -> EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Algo salió mal",
                        message = state.message,
                    )

                    StudentAttendanceUiState.NoActiveSession -> WaitingView(loading = false)

                    is StudentAttendanceUiState.AlreadySubmitted -> SubmittedView()

                    is StudentAttendanceUiState.PendingInput -> CodeInputView(
                        session = state.session,
                        errorMessage = state.errorMessage,
                        onSubmit = { viewModel.submitCode(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitingView(loading: Boolean) {
    val colors = MaterialTheme.colorScheme
    val ripple by rememberInfiniteTransition(label = "waiting").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "ripple",
    )
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val base = size.minDimension / 2f * 0.45f
                repeat(3) { i ->
                    val p = (ripple + i / 3f) % 1f
                    drawCircle(
                        color = colors.primary.copy(alpha = (1f - p) * 0.35f),
                        radius = base * (1f + p * 1.2f),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
            }
            Box(
                Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.EventAvailable,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (loading) "Buscando sesión…" else "No hay una sesión activa",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cuando tu docente active el código de asistencia, aparecerá aquí automáticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        if (!loading) {
            Spacer(Modifier.height(20.dp))
            LiveBadge()
        }
    }
}

@Composable
private fun LiveBadge() {
    val ext = IngesoftiiTheme.extendedColors
    val alpha by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "liveAlpha",
    )
    Surface(
        shape = CircleShape,
        color = ext.successContainer,
        contentColor = ext.onSuccessContainer
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .clip(CircleShape)
                    .background(ext.success),
            )
            Spacer(Modifier.width(8.dp))
            Text("En vivo · esperando al docente", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CodeInputView(
    session: AttendanceSession,
    errorMessage: String?,
    onSubmit: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    var code by rememberSaveable(session.id) { mutableStateOf("") }
    var submitting by remember(session.id) { mutableStateOf(false) }

    val remaining = rememberRemainingSeconds(session.expiresAt)
    val total = remember(session.id) { remaining.coerceAtLeast(1) }
    val expired = remaining <= 0

    LaunchedEffect(submitting) {
        if (submitting) {
            delay(SUBMIT_TIMEOUT_MS)
            submitting = false
        }
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorMessage) {
        submitting = false
        if (errorMessage != null) {
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 420
                    -14f at 60
                    14f at 120
                    -10f at 180
                    10f at 240
                    -5f at 300
                    0f at 420
                },
            )
        }
    }

    val submit = {
        if (code.isNotBlank() && !expired) {
            focusManager.clearFocus()
            submitting = true
            onSubmit(code.trim())
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        CountdownRing(
            remainingSeconds = remaining,
            totalSeconds = total,
            size = 150.dp,
            strokeWidth = 12.dp,
            timeStyle = MaterialTheme.typography.displaySmall,
            caption = if (expired) "expirado" else "para registrarte",
        )

        Spacer(Modifier.height(24.dp))

        Text(
            if (expired) "El tiempo terminó" else "¡Sesión de asistencia activa!",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (expired) "Si crees que es un error, habla con tu docente."
            else "Escribe el código que tu docente muestra en clase.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        Column(Modifier.widthIn(max = 440.dp)) {
            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    code = input.uppercase().filterNot { it.isWhitespace() }.take(MAX_CODE_LENGTH)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shake.value.dp.roundToPx(), 0) },
                enabled = !expired,
                singleLine = true,
                isError = errorMessage != null,
                placeholder = {
                    Text(
                        "CÓDIGO",
                        style = codeStyle(),
                        color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                textStyle = codeStyle().copy(
                    textAlign = TextAlign.Center,
                    color = colors.onSurface
                ),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedBorderColor = colors.primary,
                    unfocusedContainerColor = colors.surfaceContainerLow,
                    focusedContainerColor = colors.surfaceContainerLow,
                    errorContainerColor = colors.surfaceContainerLow,
                    disabledContainerColor = colors.surfaceContainerLow,
                ),
            )

            Spacer(Modifier.height(16.dp))
            ErrorBanner(errorMessage, Modifier.padding(bottom = 16.dp))

            PrimaryButton(
                text = if (expired) "Tiempo agotado" else "Registrar asistencia",
                onClick = submit,
                enabled = code.isNotBlank() && !expired,
                loading = submitting,
                leadingIcon = if (expired) Icons.Outlined.TimerOff else Icons.Rounded.Check,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun codeStyle() = MaterialTheme.typography.headlineSmall.copy(
    fontFamily = SpaceGrotesk,
    letterSpacing = 6.sp,
)

@Composable
private fun SubmittedView() {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val pop = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
    }
    LaunchedEffect(Unit) {
        delay(180)
        check.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }
    val ring by rememberInfiniteTransition(label = "doneRing").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "ring",
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val base = size.minDimension / 2f * 0.6f
                drawCircle(
                    color = ext.success.copy(alpha = (1f - ring) * 0.35f),
                    radius = base * (1f + ring * 0.6f),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            Box(
                Modifier
                    .size(104.dp)
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    }
                    .clip(CircleShape)
                    .background(ext.success),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = ext.onSuccess,
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            scaleX = check.value
                            scaleY = check.value
                            rotationZ = (1f - check.value) * -45f
                        },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "¡Asistencia registrada!",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tu docente ya puede ver que estás en clase. Puedes salir de esta pantalla.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}

private const val MAX_CODE_LENGTH = 12
private const val SUBMIT_TIMEOUT_MS = 4000L