package com.svape.unal.ingesoftii.presentation.grades

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.UserRole
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import java.util.Locale

const val MAX_GRADE = 5.0
const val PASSING_GRADE = 3.0

enum class GradeLevel { Excellent, Passing, Failing, Pending }

fun gradeLevel(grade: Double?): GradeLevel = when {
    grade == null -> GradeLevel.Pending
    grade >= 4.0 -> GradeLevel.Excellent
    grade >= PASSING_GRADE -> GradeLevel.Passing
    else -> GradeLevel.Failing
}

fun gradeLabel(level: GradeLevel): String = when (level) {
    GradeLevel.Excellent -> "Excelente"
    GradeLevel.Passing -> "Aprobada"
    GradeLevel.Failing -> "Por mejorar"
    GradeLevel.Pending -> "Pendiente"
}

fun formatGrade(grade: Double): String = String.format(Locale.US, "%.1f", grade)

@Immutable
data class GradeColors(val container: Color, val content: Color, val accent: Color)

@Composable
fun gradeColors(level: GradeLevel): GradeColors {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    return when (level) {
        GradeLevel.Excellent -> GradeColors(
            ext.successContainer,
            ext.onSuccessContainer,
            ext.success
        )

        GradeLevel.Passing -> GradeColors(
            colors.primaryContainer,
            colors.onPrimaryContainer,
            colors.primary
        )

        GradeLevel.Failing -> GradeColors(
            colors.errorContainer,
            colors.onErrorContainer,
            colors.error
        )

        GradeLevel.Pending -> GradeColors(
            colors.surfaceContainerHighest,
            colors.onSurfaceVariant,
            colors.outline
        )
    }
}

@Composable
fun GradeRing(
    grade: Double?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 6.dp,
    accent: Color = gradeColors(gradeLevel(grade)).accent,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(grade) {
        progress.animateTo(
            ((grade ?: 0.0) / MAX_GRADE).toFloat().coerceIn(0f, 1f),
            tween(900, easing = Motion.EmphasizedDecelerate),
        )
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                this.size.width - stroke,
                this.size.height - stroke
            )
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (progress.value > 0f) {
                drawArc(
                    accent,
                    -90f,
                    360f * progress.value,
                    false,
                    topLeft,
                    arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        Text(grade?.let(::formatGrade) ?: "—", style = textStyle, color = textColor)
    }
}

@Composable
fun GradePill(level: GradeLevel, modifier: Modifier = Modifier) {
    val c = gradeColors(level)
    Surface(
        shape = CircleShape,
        color = c.container,
        contentColor = c.content,
        modifier = modifier
    ) {
        Text(
            gradeLabel(level),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun StudentAvatar(
    name: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onPhotoClick: ((String) -> Unit)? = null,
) {
    ProfileAvatar(
        name = name,
        photoUrl = photoUrl,
        role = UserRole.STUDENT,
        modifier = modifier,
        size = size,
        onPhotoClick = onPhotoClick,
    )
}

fun segmentedShape(index: Int, count: Int): RoundedCornerShape {
    val outer = 22.dp
    val inner = 6.dp
    return when {
        count == 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(
            topStart = outer,
            topEnd = outer,
            bottomStart = inner,
            bottomEnd = inner
        )

        index == count - 1 -> RoundedCornerShape(
            topStart = inner,
            topEnd = inner,
            bottomStart = outer,
            bottomEnd = outer
        )

        else -> RoundedCornerShape(inner)
    }
}