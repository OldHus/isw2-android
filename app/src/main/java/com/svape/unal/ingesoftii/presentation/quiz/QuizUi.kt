package com.svape.unal.ingesoftii.presentation.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

enum class QuizShape { TRIANGLE, DIAMOND, CIRCLE, SQUARE, HEXAGON, STAR }

data class QuizOptionStyle(val color: Color, val shape: QuizShape, val shapeName: String)

private val optionPalette = listOf(
    QuizOptionStyle(Color(0xFFE0475B), QuizShape.TRIANGLE, "triángulo"),
    QuizOptionStyle(Color(0xFF0B84B8), QuizShape.DIAMOND, "rombo"),
    QuizOptionStyle(Color(0xFFC98A0B), QuizShape.CIRCLE, "círculo"),
    QuizOptionStyle(Color(0xFF1F9D6B), QuizShape.SQUARE, "cuadrado"),
    QuizOptionStyle(Color(0xFF7B5CE0), QuizShape.HEXAGON, "hexágono"),
    QuizOptionStyle(Color(0xFF0E9AA7), QuizShape.STAR, "estrella"),
)

fun quizOptionStyle(index: Int): QuizOptionStyle = optionPalette[index.mod(optionPalette.size)]

fun optionLetter(index: Int): String = ('A' + index).toString()

const val MAX_QUIZ_OPTIONS = 6

@Composable
fun QuizShapeIcon(shape: QuizShape, modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) { drawPath(shapePath(shape, size), color) }
}

private fun shapePath(shape: QuizShape, size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        when (shape) {
            QuizShape.TRIANGLE -> {
                moveTo(w / 2f, h * 0.08f)
                lineTo(w * 0.96f, h * 0.9f)
                lineTo(w * 0.04f, h * 0.9f)
                close()
            }
            QuizShape.DIAMOND -> {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }
            QuizShape.CIRCLE -> addOval(Rect(w * 0.04f, h * 0.04f, w * 0.96f, h * 0.96f))
            QuizShape.SQUARE -> addRoundRect(
                RoundRect(Rect(w * 0.1f, h * 0.1f, w * 0.9f, h * 0.9f), CornerRadius(w * 0.12f)),
            )
            QuizShape.HEXAGON -> polygon(this, w, h, points = 6, startDeg = -90.0) { 1f }
            QuizShape.STAR -> polygon(this, w, h, points = 10, startDeg = -90.0) { i -> if (i % 2 == 0) 1f else 0.45f }
        }
    }
}

private fun polygon(path: Path, w: Float, h: Float, points: Int, startDeg: Double, radiusFactor: (Int) -> Float) {
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) / 2f
    for (i in 0 until points) {
        val angle = Math.toRadians(startDeg + 360.0 / points * i)
        val rr = r * radiusFactor(i)
        val x = cx + (rr * cos(angle)).toFloat()
        val y = cy + (rr * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

@Composable
fun OptionBadge(index: Int, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val style = quizOptionStyle(index)
    Box(
        modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(style.color)
            .semantics { contentDescription = "Opción ${optionLetter(index)}, ${style.shapeName}" },
        contentAlignment = Alignment.Center,
    ) {
        QuizShapeIcon(style.shape, Modifier.size(size * 0.5f))
    }
}


@Composable
fun rememberRemainingMillis(expiresAt: Long, running: Boolean): Long {
    val state = produceState((expiresAt - System.currentTimeMillis()).coerceAtLeast(0), expiresAt, running) {
        value = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
        while (running && value > 0) {
            delay(100)
            value = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
        }
    }
    return state.value
}


@Composable
fun rememberSessionTotalMillis(sessionId: String, expiresAt: Long): Long =
    remember(sessionId) { max(expiresAt - System.currentTimeMillis(), 1_000L) }

@Composable
fun QuizTimer(
    remainingMillis: Long,
    totalMillis: Long,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 7.dp,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val seconds = ((remainingMillis + 999) / 1000).toInt()
    val fraction = (remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(fraction, tween(100, easing = LinearEasing), label = "timerFraction")
    val ringColor by animateColorAsState(
        when {
            seconds <= 5 -> colors.error
            fraction <= 0.5f -> ext.warning
            else -> colors.primary
        },
        tween(400),
        label = "timerColor",
    )

    val beat = remember { Animatable(1f) }
    LaunchedEffect(seconds) {
        if (seconds in 1..5) {
            beat.snapTo(1.12f)
            beat.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
        }
    }

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = beat.value
                scaleY = beat.value
            }
            .semantics { contentDescription = "Quedan $seconds segundos" },
        contentAlignment = Alignment.Center,
    ) {
        val track = colors.surfaceContainerHighest
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                ringColor, -90f, 360f * animatedFraction, false, Offset(inset, inset), arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Text(
            "$seconds",
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.Bold,
            color = if (seconds <= 5) colors.error else colors.onSurface,
        )
    }
}

@Composable
fun QuizLiveBadge(active: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "liveDot",
    )
    Surface(
        shape = CircleShape,
        color = if (active) colors.errorContainer else colors.surfaceContainerHighest,
        contentColor = if (active) colors.onErrorContainer else colors.onSurfaceVariant,
        modifier = modifier,
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(if (active) pulse else 1f)
                    .clip(CircleShape)
                    .background(if (active) colors.error else colors.outline),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (active) "EN VIVO" else "Cerrada",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}


@Composable
fun AnswerDistribution(
    options: List<String>,
    counts: List<Int>,
    correctIndex: Int,
    revealCorrect: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val maxCount = max(counts.maxOrNull() ?: 0, 1)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { index, option ->
            val count = counts.getOrElse(index) { 0 }
            val isCorrect = index == correctIndex
            val dim = revealCorrect && !isCorrect
            val barFraction by animateFloatAsState(
                count.toFloat() / maxCount,
                spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
                label = "bar$index",
            )
            val animatedCount by animateIntAsState(count, tween(500), label = "count$index")
            Column(Modifier.alpha(if (dim) 0.45f else 1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OptionBadge(index, size = 24.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        option,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (revealCorrect && isCorrect) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = "Respuesta correcta",
                            tint = ext.success,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(18.dp),
                        )
                    }
                    Text(
                        "$animatedCount",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHighest),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(barFraction)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(quizOptionStyle(index).color),
                    )
                }
            }
        }
    }
}



fun formatResponseTime(millis: Long): String =
    String.format(Locale("es", "CO"), "%.1f s", millis / 1000.0)


@Composable
fun QuizPodium(entries: List<QuizLeaderboardEntry>, modifier: Modifier = Modifier) {
    val top = entries.take(3)
    if (top.isEmpty()) return
    val order = listOfNotNull(top.getOrNull(1), top.getOrNull(0), top.getOrNull(2))
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        order.forEach { entry ->
            PodiumColumn(entry, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PodiumColumn(entry: QuizLeaderboardEntry, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (medal, height, tint) = when (entry.rank) {
        1 -> Triple("🥇", 124.dp, Color(0xFFE8B931))
        2 -> Triple("🥈", 92.dp, Color(0xFFA7B1C2))
        else -> Triple("🥉", 68.dp, Color(0xFFCD8B5B))
    }
    val grow = remember(entry.studentName, entry.rank) { Animatable(0f) }
    LaunchedEffect(entry.studentName, entry.rank) {
        delay((3 - entry.rank) * 150L)
        grow.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(medal, fontSize = 26.sp)
        ProfileAvatar(name = entry.studentName, photoUrl = null, role = null, size = 44.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            entry.studentName.trim().substringBefore(' ').ifBlank { "Estudiante" },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text("${entry.score} pts", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(height * grow.value)
                .clip(MaterialTheme.shapes.medium.copy(bottomStart = CornerRadiusZero, bottomEnd = CornerRadiusZero))
                .background(tint.copy(alpha = 0.85f)),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                "${entry.rank}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private val CornerRadiusZero = androidx.compose.foundation.shape.CornerSize(0.dp)


@Composable
fun LeaderboardRow(entry: QuizLeaderboardEntry, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val animatedScore by animateIntAsState(entry.score, tween(700), label = "score")
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${entry.rank}",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
        )
        ProfileAvatar(name = entry.studentName, photoUrl = null, role = null, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.studentName.ifBlank { "Estudiante" },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (entry.isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                    contentDescription = null,
                    tint = if (entry.isCorrect) ext.success else colors.error,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    (if (entry.isCorrect) "Correcta" else "Incorrecta") + " · " + formatResponseTime(entry.responseTimeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
        Text(
            "$animatedScore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            " pts",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
        )
    }
}


private data class ConfettiPiece(
    val x: Float, val vx: Float, val vy: Float, val size: Float,
    val rotation: Float, val spin: Float, val color: Color,
)


@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, pieces: Int = 60) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2200, easing = LinearEasing)) }
    val particles = remember {
        val random = Random(System.nanoTime())
        List(pieces) {
            ConfettiPiece(
                x = random.nextFloat(),
                vx = (random.nextFloat() - 0.5f) * 0.5f,
                vy = -(0.5f + random.nextFloat() * 0.6f),
                size = 6f + random.nextFloat() * 8f,
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                color = optionPalette[random.nextInt(optionPalette.size)].color,
            )
        }
    }
    if (progress.value >= 1f) return
    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f
        particles.forEach { p ->
            val x = (p.x + p.vx * t) * size.width
            val y = size.height * (0.55f + p.vy * t + 1.6f * t * t)
            rotate(p.rotation + p.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.size / 2, y - p.size / 4),
                    size = Size(p.size, p.size / 2),
                )
            }
        }
    }
}