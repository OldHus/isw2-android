package com.svape.unal.ingesoftii.presentation.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import kotlinx.coroutines.delay

@Composable
fun rememberRemainingSeconds(expiresAtMillis: Long): Long {
    fun compute() = ((expiresAtMillis - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
    var remaining by remember(expiresAtMillis) { mutableLongStateOf(compute()) }
    LaunchedEffect(expiresAtMillis) {
        while (remaining > 0) {
            delay(1000 - (System.currentTimeMillis() % 1000))
            remaining = compute()
        }
    }
    return remaining
}

fun formatCountdown(seconds: Long): String =
    "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

@Composable
fun countdownColor(remainingSeconds: Long): Color {
    val colors = MaterialTheme.colorScheme
    val target = when {
        remainingSeconds <= 15 -> colors.error
        remainingSeconds <= 60 -> IngesoftiiTheme.extendedColors.warning
        else -> colors.primary
    }
    return animateColorAsState(target, tween(400), label = "countdownColor").value
}

@Composable
fun CountdownRing(
    remainingSeconds: Long,
    totalSeconds: Long,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    color: Color = countdownColor(remainingSeconds),
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    timeStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    caption: String? = "restante",
) {
    val fraction =
        if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    val animated by animateFloatAsState(fraction, tween(900), label = "countdownFraction")

    val pulse by rememberInfiniteTransition(label = "countdownPulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulse",
    )
    val urgent = remainingSeconds in 1..10

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                val scale = if (urgent) pulse else 1f
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arc = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(trackColor, 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            if (animated > 0f) {
                drawArc(
                    color,
                    -90f,
                    360f * animated,
                    false,
                    topLeft,
                    arc,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (remainingSeconds > 0) formatCountdown(remainingSeconds) else "0:00",
                style = timeStyle,
                color = textColor,
            )
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}