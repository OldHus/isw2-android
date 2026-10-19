package com.svape.unal.ingesoftii.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

val LocalThemeToggle = staticCompositionLocalOf<(Offset) -> Unit> { {} }

@Composable
fun ThemeRevealHost(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Int = 550,
    content: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(1f) }
    var snapshot by remember { mutableStateOf<ImageBitmap?>(null) }
    var origin by remember { mutableStateOf(Offset.Unspecified) }
    val currentOnToggle by rememberUpdatedState(onToggleTheme)

    val toggle: (Offset) -> Unit = remember(layer) {
        { center ->
            if (snapshot == null) {
                scope.launch {
                    val bitmap = runCatching { layer.toImageBitmap() }.getOrNull()
                    if (bitmap == null) {
                        currentOnToggle()
                        return@launch
                    }
                    origin = center
                    snapshot = bitmap
                    progress.snapTo(0f)
                    currentOnToggle()
                    progress.animateTo(1f, tween(durationMillis, easing = Motion.Emphasized))
                    snapshot = null
                }
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                },
        ) {
            CompositionLocalProvider(LocalThemeToggle provides toggle) { content() }
        }

        val old = snapshot
        if (old != null) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val o = if (origin.isSpecified) origin else center
                        val maxRadius =
                            hypot(max(o.x, size.width - o.x), max(o.y, size.height - o.y))
                        val hole = Path().apply { addOval(Rect(o, maxRadius * progress.value)) }
                        clipPath(hole, clipOp = ClipOp.Difference) { drawImage(old) }
                    },
            )
        }
    }
}