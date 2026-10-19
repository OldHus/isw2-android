package com.svape.unal.ingesoftii.presentation.quiz.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.domain.model.QuizAnswer
import com.svape.unal.ingesoftii.domain.model.QuizSession
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.presentation.quiz.ConfettiBurst
import com.svape.unal.ingesoftii.presentation.quiz.LeaderboardRow
import com.svape.unal.ingesoftii.presentation.quiz.QuizLeaderboardEntry
import com.svape.unal.ingesoftii.presentation.quiz.QuizLiveBadge
import com.svape.unal.ingesoftii.presentation.quiz.QuizPodium
import com.svape.unal.ingesoftii.presentation.quiz.QuizShapeIcon
import com.svape.unal.ingesoftii.presentation.quiz.QuizStandingCard
import com.svape.unal.ingesoftii.presentation.quiz.QuizTimer
import com.svape.unal.ingesoftii.presentation.quiz.formatResponseTime
import com.svape.unal.ingesoftii.presentation.quiz.quizOptionStyle
import com.svape.unal.ingesoftii.presentation.quiz.rememberRemainingMillis
import com.svape.unal.ingesoftii.presentation.quiz.rememberSessionTotalMillis
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.pressScale
import kotlinx.coroutines.delay

private const val QUESTION_RANKING_SIZE = 5

private enum class Phase { Answering, Answered, TimeUp, Result }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuizScreen(
    courseId: String,
    viewModel: StudentQuizViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val standing by viewModel.standing.collectAsState()
    val questionRanking by viewModel.questionRanking.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(courseId) { viewModel.start(courseId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "Quiz en vivo") },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                StudentQuizUiState.Loading -> LoadingList(header = true, rows = 4, rowHeight = 80.dp)

                is StudentQuizUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos cargar el quiz",
                        message = state.message,
                        action = { TextButton(onClick = { viewModel.start(courseId) }) { Text("Reintentar") } },
                    )
                }

                StudentQuizUiState.NoActiveQuestion -> WaitingForQuestion(standing)

                is StudentQuizUiState.Question -> QuestionScreen(
                    session = state.session,
                    myAnswer = state.myAnswer,
                    standing = standing,
                    questionRanking = questionRanking,
                    snackbarHostState = snackbarHostState,
                    onAnswer = { viewModel.answer(it) },
                )
            }
        }
    }
}


@Composable
private fun WaitingForQuestion(standing: List<QuizStandingEntry>) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "waiting")
    val ring by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2000)), label = "ring")
    val float by transition.animateFloat(-6f, 6f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "float")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                listOf(0f, 0.5f).forEach { offset ->
                    val p = (ring + offset) % 1f
                    Box(
                        Modifier
                            .size(180.dp)
                            .scale(0.45f + 0.55f * p)
                            .alpha(1f - p)
                            .border(2.dp, colors.primary.copy(alpha = 0.6f), CircleShape),
                    )
                }
                Box(
                    Modifier
                        .size(88.dp)
                        .graphicsLayer { translationY = float }
                        .clip(CircleShape)
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Quiz, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Esperando la próxima pregunta", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Cuando tu docente lance una pregunta aparecerá aquí al instante. Mantén esta pantalla abierta.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bolt, contentDescription = null, tint = IngesoftiiTheme.extendedColors.warning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Entre más rápido aciertes, más puntos ganas", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (standing.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            QuizStandingCard(standing)
        }
    }
}


@Composable
private fun QuestionScreen(
    session: QuizSession,
    myAnswer: QuizAnswer?,
    standing: List<QuizStandingEntry>,
    questionRanking: List<QuizLeaderboardEntry>,
    snackbarHostState: SnackbarHostState,
    onAnswer: (Int) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val remaining = rememberRemainingMillis(session.expiresAt, session.isActive)
    val total = rememberSessionTotalMillis(session.id, session.expiresAt)

    var pendingIndex by rememberSaveable(session.id) { mutableStateOf<Int?>(null) }
    LaunchedEffect(pendingIndex, myAnswer) {
        if (pendingIndex != null && myAnswer == null) {
            delay(6_000)
            pendingIndex = null
            snackbarHostState.showSnackbar("No se pudo enviar tu respuesta. Intenta de nuevo.")
        }
    }

    val phase = when {
        !session.isActive -> Phase.Result
        myAnswer != null -> Phase.Answered
        pendingIndex != null -> Phase.Answering
        remaining <= 0L -> Phase.TimeUp
        else -> Phase.Answering
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = IngesoftiiTheme.extendedColors.card,
            border = BorderStroke(1.dp, IngesoftiiTheme.extendedColors.cardBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    QuizLiveBadge(active = session.isActive)
                    Spacer(Modifier.height(10.dp))
                    Text(session.questionText, style = MaterialTheme.typography.titleLarge)
                }
                if (session.isActive && phase != Phase.TimeUp) {
                    Spacer(Modifier.width(12.dp))
                    QuizTimer(remainingMillis = remaining, totalMillis = total)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                (fadeIn(tween(250)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(150))
            },
            label = "quizPhase",
        ) { current ->
            when (current) {
                Phase.Answering -> OptionsGrid(
                    options = session.options,
                    pendingIndex = pendingIndex,
                    onPick = { index ->
                        if (pendingIndex == null && remaining > 0L) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            pendingIndex = index
                            onAnswer(index)
                        }
                    },
                )

                Phase.Answered -> AnsweredView(
                    selectedIndex = myAnswer?.selectedOptionIndex ?: pendingIndex ?: 0,
                    option = session.options.getOrNull(myAnswer?.selectedOptionIndex ?: -1).orEmpty(),
                    responseTimeMillis = myAnswer?.takeIf { it.isCorrect != null }?.responseTimeMillis,
                )

                Phase.TimeUp -> StatusView(
                    emoji = "⏰",
                    title = "¡Se acabó el tiempo!",
                    message = "No alcanzaste a responder. Prepárate para la siguiente.",
                )

                Phase.Result -> ResultView(session = session, myAnswer = myAnswer)
            }
        }
        if (phase == Phase.Result && questionRanking.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            QuestionRankingCard(questionRanking)
        }
        if (phase == Phase.Result && standing.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            QuizStandingCard(standing)
        }
        Spacer(Modifier.height(24.dp))
    }
}


@Composable
private fun QuestionRankingCard(ranking: List<QuizLeaderboardEntry>) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val winners = ranking.filter { it.isCorrect }.take(QUESTION_RANKING_SIZE)

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Mejores puntajes de esta pregunta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                if (ranking.size == 1) "1 respuesta" else "${ranking.size} respuestas",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            if (winners.isEmpty()) {
                Text(
                    "Nadie acertó esta pregunta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            } else {
                QuizPodium(winners)
                if (winners.size > 3) {
                    Spacer(Modifier.height(8.dp))
                    winners.drop(3).forEach { LeaderboardRow(it) }
                }
            }
        }
    }
}

@Composable
private fun OptionsGrid(options: List<String>, pendingIndex: Int?, onPick: (Int) -> Unit) {
    val twoColumns = options.size <= 4 && options.all { it.length <= 40 }
    val rows = if (twoColumns) options.indices.chunked(2) else options.indices.map { listOf(it) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowIndices ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowIndices.forEach { index ->
                    OptionTile(
                        index = index,
                        text = options[index],
                        state = when {
                            pendingIndex == null -> TileState.Idle
                            pendingIndex == index -> TileState.Sending
                            else -> TileState.Faded
                        },
                        tall = twoColumns,
                        onClick = { onPick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (twoColumns && rowIndices.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private enum class TileState { Idle, Sending, Faded }

@Composable
private fun OptionTile(
    index: Int,
    text: String,
    state: TileState,
    tall: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = quizOptionStyle(index)
    val interaction = remember { MutableInteractionSource() }

    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        appear.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
    }
    val alpha by animateFloatAsState(if (state == TileState.Faded) 0.3f else 1f, label = "tileAlpha")
    val selectedScale by animateFloatAsState(if (state == TileState.Sending) 1.03f else 1f, label = "tileScale")

    Surface(
        onClick = onClick,
        enabled = state == TileState.Idle,
        interactionSource = interaction,
        shape = MaterialTheme.shapes.large,
        color = style.color,
        contentColor = Color.White,
        shadowElevation = if (state == TileState.Sending) 8.dp else 2.dp,
        modifier = modifier
            .heightIn(min = if (tall) 120.dp else 68.dp)
            .graphicsLayer {
                val s = appear.value * selectedScale
                scaleX = s
                scaleY = s
                this.alpha = alpha * appear.value.coerceIn(0f, 1f)
            }
            .pressScale(interaction, pressedScale = 0.95f),
    ) {
        val content: @Composable () -> Unit = {
            if (state == TileState.Sending) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            } else {
                QuizShapeIcon(style.shape, Modifier.size(if (tall) 30.dp else 24.dp))
            }
        }
        if (tall) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                content()
                Spacer(Modifier.height(12.dp))
                Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
            }
        } else {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                content()
                Spacer(Modifier.width(14.dp))
                Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


@Composable
private fun AnsweredView(selectedIndex: Int, option: String, responseTimeMillis: Long?) {
    val colors = MaterialTheme.colorScheme
    val style = quizOptionStyle(selectedIndex)
    val bob by rememberInfiniteTransition(label = "answered").animateFloat(
        -4f, 4f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "bob",
    )
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .size(96.dp)
                .graphicsLayer { translationY = bob }
                .clip(MaterialTheme.shapes.extraLarge)
                .background(style.color),
            contentAlignment = Alignment.Center,
        ) {
            QuizShapeIcon(style.shape, Modifier.size(44.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("¡Respuesta enviada!", style = MaterialTheme.typography.headlineSmall)
        if (option.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Elegiste: $option",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (responseTimeMillis != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Respondiste en ${formatResponseTime(responseTimeMillis)}",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "¿Acertaste? Lo sabrás cuando se cierre la pregunta 🤞",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatusView(emoji: String, title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
private fun ResultView(session: QuizSession, myAnswer: QuizAnswer?) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val graded: Boolean? = myAnswer?.let { answer ->
        answer.isCorrect ?: session.correctOptionIndex?.let { answer.selectedOptionIndex == it }
    }
    val correct = graded == true
    val grading = myAnswer != null && graded == null
    val correctIndex = session.correctOptionIndex ?: myAnswer?.selectedOptionIndex?.takeIf { correct }

    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) }
    val score by animateIntAsState(if (correct) myAnswer?.score ?: 0 else 0, tween(900), label = "score")

    val (container, content) = when {
        correct -> ext.successContainer to ext.onSuccessContainer
        grading -> colors.surfaceContainerHigh to colors.onSurface
        myAnswer != null -> colors.errorContainer to colors.onErrorContainer
        else -> colors.surfaceContainerHigh to colors.onSurface
    }

    Box {
        Column {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = container,
                contentColor = content,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = pop.value
                        scaleY = pop.value
                    },
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when {
                            correct -> "🎉"
                            grading -> "⏳"
                            myAnswer != null -> "😅"
                            else -> "⏰"
                        },
                        fontSize = 52.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            correct -> "¡Correcto!"
                            grading -> "Calificando…"
                            myAnswer != null -> "Casi…"
                            else -> "No respondiste"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    when {
                        correct -> {
                            Text("+$score pts", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            myAnswer?.let {
                                Text(
                                    "Respondiste en ${formatResponseTime(it.responseTimeMillis)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        grading -> Text(
                            "Tu resultado aparece en un momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        myAnswer != null -> Text(
                            "Esta vez no fue. ¡En la próxima te va mejor!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        else -> Text(
                            "Esta pregunta ya se cerró.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Respuestas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                session.options.forEachIndexed { index, option ->
                    ReviewRow(
                        index = index,
                        text = option,
                        isCorrect = index == correctIndex,
                        isMine = !grading && myAnswer?.selectedOptionIndex == index,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Espera la siguiente pregunta de tu docente 👀",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (correct) ConfettiBurst(Modifier.matchParentSize())
    }
}

@Composable
private fun ReviewRow(index: Int, text: String, isCorrect: Boolean, isMine: Boolean) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val style = quizOptionStyle(index)
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(300L + index * 70L)
        appear.animateTo(1f, tween(300))
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = when {
            isCorrect -> ext.successContainer
            isMine -> colors.errorContainer
            else -> colors.surfaceContainer
        },
        border = if (isCorrect) BorderStroke(2.dp, ext.success) else null,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 16f
            },
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(style.color.copy(alpha = if (isCorrect || isMine) 1f else 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                QuizShapeIcon(style.shape, Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isCorrect -> ext.onSuccessContainer
                    isMine -> colors.onErrorContainer
                    else -> colors.onSurfaceVariant
                },
                modifier = Modifier.weight(1f),
            )
            if (isMine) {
                Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.7f)) {
                    Text(
                        "Tú",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            when {
                isCorrect -> Icon(Icons.Rounded.CheckCircle, contentDescription = "Correcta", tint = ext.success)
                isMine -> Icon(Icons.Rounded.Cancel, contentDescription = "Incorrecta", tint = colors.error)
            }
        }
    }
}