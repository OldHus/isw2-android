package com.svape.unal.ingesoftii.presentation.course

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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.ui.components.ErrorBanner
import com.svape.unal.ingesoftii.ui.components.PrimaryButton
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.SpaceGrotesk
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun CourseCodeScreen(
    viewModel: CourseCodeViewModel,
    onRegistrationSuccess: (role: String, courseId: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is CourseCodeUiState.Success) {
            delay(1300)
            onRegistrationSuccess(state.role, state.courseId)
        }
    }

    CourseCodeContent(
        code = code,
        onCodeChange = { input ->
            code = input.uppercase().filterNot { it.isWhitespace() }.take(24)
        },
        onSubmit = { viewModel.onSubmitCode(code) },
        isLoading = uiState is CourseCodeUiState.Loading,
        errorMessage = (uiState as? CourseCodeUiState.Error)?.message,
        isSuccess = uiState is CourseCodeUiState.Success,
    )
}

@Composable
fun CourseCodeContent(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isSuccess: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        TopGlow()

        AnimatedContent(
            targetState = isSuccess,
            transitionSpec = {
                (fadeIn(tween(Motion.Long)) + scaleIn(tween(Motion.Long, easing = Motion.EmphasizedDecelerate), initialScale = 0.92f)) togetherWith
                        fadeOut(tween(Motion.Short))
            },
            label = "courseCodeState",
        ) { success ->
            if (success) {
                SuccessView()
            } else {
                FormView(
                    code = code,
                    onCodeChange = onCodeChange,
                    onSubmit = onSubmit,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}

@Composable
private fun FormView(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorMessage) {
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
        focusManager.clearFocus()
        onSubmit()
    }

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))

        StaggeredEnter(index = 0) { KeyEmblem() }

        Spacer(Modifier.height(20.dp))

        StaggeredEnter(index = 1) {
            Text(
                "Únete a tu curso",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.onBackground,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(8.dp))
        StaggeredEnter(index = 2) {
            Text(
                "Ingresa el código que te compartió tu docente para acceder a Ingesoft II.",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 340.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        StaggeredEnter(index = 3) {
            Column(Modifier.widthIn(max = 440.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(shake.value.dp.roundToPx(), 0) },
                    enabled = !isLoading,
                    singleLine = true,
                    isError = errorMessage != null,
                    placeholder = {
                        Text(
                            "CÓDIGO",
                            style = codeTextStyle(),
                            color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                    textStyle = codeTextStyle().copy(textAlign = TextAlign.Center, color = colors.onSurface),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (code.isNotBlank()) submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colors.outlineVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        focusedContainerColor = colors.surfaceContainerLow,
                        errorContainerColor = colors.surfaceContainerLow,
                        disabledContainerColor = colors.surfaceContainerLow,
                        focusedLeadingIconColor = colors.primary,
                        unfocusedLeadingIconColor = colors.onSurfaceVariant,
                    ),
                )

                Spacer(Modifier.height(16.dp))
                ErrorBanner(errorMessage, Modifier.padding(bottom = 16.dp))

                PrimaryButton(
                    text = "Unirme al curso",
                    onClick = submit,
                    enabled = code.isNotBlank(),
                    loading = isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        StaggeredEnter(index = 4) { HelpCard(Modifier.widthIn(max = 440.dp)) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun codeTextStyle() = MaterialTheme.typography.headlineSmall.copy(
    fontFamily = SpaceGrotesk,
    letterSpacing = 4.sp,
)

@Composable
private fun KeyEmblem() {
    val colors = MaterialTheme.colorScheme
    val breathe by rememberInfiniteTransition(label = "keyHalo").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "halo",
    )
    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = size.minDimension / 2f * 0.62f
            repeat(2) { i ->
                val p = (breathe + i * 0.5f) % 1f
                drawCircle(
                    color = colors.primary.copy(alpha = (1f - p) * 0.3f),
                    radius = base * (1f + p * 0.6f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
        Box(
            Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.primary, colors.onPrimaryContainer))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Key, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(38.dp))
        }
    }
}

@Composable
private fun HelpCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainer,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("¿Dónde encuentro mi código?", style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tu docente lo comparte en clase o por correo. Cada código es personal y solo se puede usar una vez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SuccessView() {
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
    val ring by rememberInfiniteTransition(label = "successRing").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "ring",
    )

    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val base = size.minDimension / 2f * 0.6f
                drawCircle(
                    color = ext.success.copy(alpha = (1f - ring) * 0.35f),
                    radius = base * (1f + ring * 0.65f),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            Box(
                Modifier
                    .size(96.dp)
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
                        .size(52.dp)
                        .graphicsLayer {
                            scaleX = check.value
                            scaleY = check.value
                            rotationZ = (1f - check.value) * -45f
                        },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("¡Te uniste al curso!", style = MaterialTheme.typography.headlineMedium, color = colors.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(
            "Preparando tu espacio…",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun TopGlow() {
    val glow = IngesoftiiTheme.extendedColors.glow
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            Brush.radialGradient(
                listOf(glow, Color.Transparent),
                center = center.copy(y = size.height * 0.12f),
                radius = size.width * 0.9f,
            ),
            radius = size.width * 0.9f,
            center = center.copy(y = size.height * 0.12f),
        )
    }
}

@PreviewLightDark
@Composable
private fun CourseCodePreview() {
    IngesoftiiTheme {
        CourseCodeContent(code = "ING2-7XK4", onCodeChange = {}, onSubmit = {})
    }
}

@PreviewLightDark
@Composable
private fun CourseCodeSuccessPreview() {
    IngesoftiiTheme {
        CourseCodeContent(code = "", onCodeChange = {}, onSubmit = {}, isSuccess = true)
    }
}