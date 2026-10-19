package com.svape.unal.ingesoftii.presentation.auth

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.BuildConfig
import com.svape.unal.ingesoftii.R
import com.svape.unal.ingesoftii.ui.components.AppTextField
import com.svape.unal.ingesoftii.ui.components.ErrorBanner
import com.svape.unal.ingesoftii.ui.components.IconBubble
import com.svape.unal.ingesoftii.ui.components.PasswordTextField
import com.svape.unal.ingesoftii.ui.components.PrimaryButton
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.LocalIsDarkTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter
import com.svape.unal.ingesoftii.ui.theme.pressScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onExistingUser: (role: String, courseId: String) -> Unit,
    onNewUser: (uid: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showReviewerAccess by rememberSaveable { mutableStateOf(false) }
    var reviewerEmail by rememberSaveable { mutableStateOf("") }
    var reviewerPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.ExistingUserSuccess -> onExistingUser(state.role, state.courseId)
            is LoginUiState.NewUserSuccess -> onNewUser(state.uid)
            else -> Unit
        }
    }

    val isLoading = uiState is LoginUiState.Loading
    val errorMessage = (uiState as? LoginUiState.Error)?.message

    LoginContent(
        onGoogleSignIn = viewModel::onSignInClicked,
        onReviewerAccess = { showReviewerAccess = true },
        isLoading = isLoading && !showReviewerAccess,
        errorMessage = if (showReviewerAccess) null else errorMessage,
    )

    if (showReviewerAccess) {
        ReviewerAccessSheet(
            email = reviewerEmail,
            onEmailChange = { reviewerEmail = it },
            password = reviewerPassword,
            onPasswordChange = { reviewerPassword = it },
            isLoading = isLoading,
            errorMessage = errorMessage,
            onSubmit = {
                viewModel.onSignInWithEmailClicked(
                    reviewerEmail.trim(),
                    reviewerPassword
                )
            },
            onDismiss = { if (!isLoading) showReviewerAccess = false },
        )
    }
}

@Composable
fun LoginContent(
    onGoogleSignIn: () -> Unit,
    onReviewerAccess: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPrivacyPolicy: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        AuroraBackground()

        Column(
            Modifier
                .align(Alignment.Center)
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            BrandEmblem()

            Spacer(Modifier.height(8.dp))

            StaggeredEnter(index = 2) {
                Text(
                    "BIENVENIDO A",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.5.sp),
                    color = colors.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            StaggeredEnter(index = 3) {
                Text(
                    text = buildAnnotatedString {
                        append("Ingesoft ")
                        withStyle(
                            SpanStyle(
                                brush = Brush.linearGradient(
                                    listOf(
                                        BrandCyan,
                                        BrandBlue
                                    )
                                )
                            )
                        ) { append("II") }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
            StaggeredEnter(index = 4) {
                Text(
                    "Tu curso en un solo lugar: notas, proyecto, asistencia y mucho más.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 320.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            StaggeredEnter(index = 5) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeaturePill(Icons.Outlined.School, "Notas")
                    FeaturePill(Icons.Outlined.QrCode2, "Asistencia")
                    FeaturePill(Icons.Outlined.EmojiEvents, "Quiz")
                }
            }

            Spacer(Modifier.weight(1.2f))

            ErrorBanner(errorMessage, Modifier.padding(bottom = 16.dp))

            StaggeredEnter(index = 6) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GoogleHeroButton(onClick = onGoogleSignIn, loading = isLoading)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Usa tu correo institucional @unal.edu.co",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            StaggeredEnter(index = 7) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = onReviewerAccess, enabled = !isLoading) {
                        Text("Acceso para revisores", style = MaterialTheme.typography.labelLarge)
                    }
                    if (onPrivacyPolicy != null) {
                        PrivacyText(onPrivacyPolicy)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Versión ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewerAccessSheet(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            IconBubble(Icons.Outlined.AdminPanelSettings, size = 52.dp)
            Spacer(Modifier.height(16.dp))
            Text("Acceso para revisores", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Solo para el equipo de revisión de Google Play y cuentas de prueba.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            AppTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Correo",
                leadingIcon = Icons.Outlined.Mail,
                enabled = !isLoading,
                autofillType = ContentType.EmailAddress,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            PasswordTextField(
                value = password,
                onValueChange = onPasswordChange,
                enabled = !isLoading,
                onImeAction = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            ErrorBanner(errorMessage, Modifier.padding(bottom = 16.dp))

            PrimaryButton(
                text = "Ingresar",
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                loading = isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val BrandCyan = Color(0xFF4CC9F0)
private val BrandBlue = Color(0xFF0B84B8)
private val BrandAmber = Color(0xFFF5B971)
private val BrandNavy = Color(0xFF011252)
private val BrandNavyLight = Color(0xFF0A2A7A)


private fun ingesoftLogoPath(): Path = Path().apply {
    moveTo(365f, 346f)
    lineTo(525f, 346f)
    lineTo(525f, 660f)
    cubicTo(525f, 725f, 570f, 775f, 627f, 775f)
    cubicTo(685f, 775f, 728f, 725f, 728f, 660f)
    lineTo(728f, 346f)
    lineTo(888f, 346f)
    lineTo(888f, 680f)
    cubicTo(888f, 830f, 790f, 918f, 627f, 918f)
    cubicTo(480f, 918f, 380f, 840f, 365f, 712f)
    cubicTo(375f, 722f, 385f, 730f, 398f, 737f)
    lineTo(568f, 832f)
    cubicTo(565f, 810f, 545f, 790f, 520f, 774f)
    lineTo(398f, 693f)
    cubicTo(380f, 682f, 365f, 665f, 365f, 640f)
    close()
}


@Composable
fun BrandEmblem(modifier: Modifier = Modifier, emblemSize: Dp = 108.dp) {
    val appear = remember { Animatable(0f) }
    val draw = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            appear.animateTo(
                1f,
                spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
            )
        }
        delay(250)
        draw.animateTo(1f, tween(1400, easing = Motion.EmphasizedDecelerate))
    }

    val logo = remember { ingesoftLogoPath() }
    val measure = remember(logo) { PathMeasure().apply { setPath(logo, true) } }

    val infinite = rememberInfiniteTransition(label = "emblem")
    val float by infinite.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float",
    )
    val ripple by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "ripple",
    )
    val spin by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "spin",
    )

    val shape = RoundedCornerShape(emblemSize * 0.24f)

    Box(modifier.size(emblemSize * 2f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = emblemSize.toPx() / 2f
            val a = appear.value.coerceIn(0f, 1f)
            drawCircle(
                Brush.radialGradient(
                    listOf(BrandCyan.copy(alpha = 0.30f * a), Color.Transparent),
                    center,
                    base * 1.9f
                ),
                radius = base * 1.9f,
            )
            repeat(2) { i ->
                val p = (ripple + i * 0.5f) % 1f
                drawCircle(
                    color = BrandCyan.copy(alpha = (1f - p) * 0.35f * a),
                    radius = base * (1.05f + p * 0.85f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }

        Box(
            Modifier
                .size(emblemSize)
                .graphicsLayer {
                    val a = appear.value
                    alpha = a.coerceIn(0f, 1f)
                    scaleX = 0.6f + 0.4f * a
                    scaleY = 0.6f + 0.4f * a
                    rotationZ = (1f - a) * -14f
                    translationY = float * 5.dp.toPx()
                }
                .clip(shape)
                .drawBehind {
                    rotate(spin) {
                        drawCircle(
                            Brush.sweepGradient(
                                listOf(
                                    BrandCyan.copy(alpha = 0.15f),
                                    BrandCyan,
                                    Color.White,
                                    BrandCyan.copy(alpha = 0.15f)
                                ),
                            ),
                            radius = size.maxDimension,
                        )
                    }
                }
                .padding(2.5.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(BrandNavyLight, BrandNavy))),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val k = size.width / 1254f * 1.15f
                val progress = draw.value
                val fillAlpha = ((progress - 0.55f) / 0.45f).coerceIn(0f, 1f)

                withTransform({
                    translate(
                        left = size.width / 2f - 626.5f * k,
                        top = size.height / 2f - 632f * k
                    )
                    scale(scaleX = k, scaleY = k, pivot = Offset.Zero)
                }) {
                    if (progress < 1f) {
                        val segment = Path()
                        measure.getSegment(0f, measure.length * progress, segment, true)
                        drawPath(
                            segment,
                            color = Color.White,
                            style = Stroke(
                                width = 2.dp.toPx() / k,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                        )
                    }
                    drawPath(logo, color = Color.White.copy(alpha = fillAlpha))
                }

                drawRect(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        ), endY = size.height * 0.5f
                    ),
                )
            }
        }
    }
}


@Composable
private fun AuroraBackground() {
    val colors = MaterialTheme.colorScheme
    val isDark = LocalIsDarkTheme.current
    val t by rememberInfiniteTransition(label = "aurora").animateFloat(
        0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "t",
    )
    val dotColor = colors.onBackground.copy(alpha = if (isDark) 0.06f else 0.07f)
    val background = colors.background

    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val step = 22.dp.toPx()
                val maxY = size.height * 0.7f
                val maxX = size.width
                val points = buildList {
                    var y = step / 2
                    while (y < maxY) {
                        var x = step / 2
                        while (x < maxX) {
                            add(Offset(x, y)); x += step
                        }
                        y += step
                    }
                }
                val fade = Brush.verticalGradient(
                    0f to Color.Transparent, 0.35f to Color.Transparent, 0.7f to background,
                )
                onDrawBehind {
                    drawPoints(
                        points,
                        PointMode.Points,
                        dotColor,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawRect(fade)
                }
            },
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val a = t * 2f * PI.toFloat()
        fun blob(cx: Float, cy: Float, r: Float, c: Color) = drawCircle(
            Brush.radialGradient(listOf(c, Color.Transparent), Offset(cx, cy), r),
            radius = r,
            center = Offset(cx, cy),
        )
        blob(
            w * (0.25f + 0.12f * cos(a)),
            h * (0.22f + 0.05f * sin(a)),
            w * 0.85f,
            BrandCyan.copy(alpha = if (isDark) 0.16f else 0.14f)
        )
        blob(
            w * (0.85f + 0.08f * sin(a)),
            h * (0.40f + 0.06f * cos(a)),
            w * 0.65f,
            BrandAmber.copy(alpha = if (isDark) 0.08f else 0.12f)
        )
        blob(
            w * (0.10f + 0.06f * sin(a)),
            h * (0.95f + 0.03f * cos(a)),
            w * 0.70f,
            BrandBlue.copy(alpha = if (isDark) 0.12f else 0.08f)
        )
    }
}


@Composable
private fun FeaturePill(icon: ImageVector, label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun GoogleHeroButton(onClick: () -> Unit, loading: Boolean) {
    val interaction = remember { MutableInteractionSource() }
    val shine by rememberInfiniteTransition(label = "shine").animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4000
                -0.5f at 0
                -0.5f at 2200
                1.5f at 3200 using FastOutSlowInEasing
                1.5f at 4000
            },
        ),
        label = "shineX",
    )
    Button(
        onClick = { if (!loading) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .pressScale(interaction)
            .clip(CircleShape)
            .drawWithContent {
                drawContent()
                if (!loading) {
                    val x = size.width * shine
                    drawRect(
                        Brush.linearGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.28f),
                                Color.Transparent
                            ),
                            start = Offset(x - size.height * 1.5f, 0f),
                            end = Offset(x, size.height),
                        ),
                    )
                }
            },
        shape = CircleShape,
        interactionSource = interaction,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = BrandBlue
                )
            } else {
                Image(
                    painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            if (loading) "Conectando…" else "Continuar con Google",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(44.dp))
    }
}

@Composable
private fun PrivacyText(onPrivacyPolicy: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val text = buildAnnotatedString {
        append("Al continuar, aceptas nuestra ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacy",
                styles = TextLinkStyles(
                    SpanStyle(
                        color = colors.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
            ) { onPrivacyPolicy() },
        ) { append("política de privacidad") }
        append(".")
    }
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@PreviewLightDark
@Composable
private fun LoginContentPreview() {
    IngesoftiiTheme {
        LoginContent(onGoogleSignIn = {}, onReviewerAccess = {})
    }
}