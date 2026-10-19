package com.svape.unal.ingesoftii.presentation.session

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

private val SplashNavy = Color(0xFF011252)

@Composable
fun SplashScreen() {
    ForceLightSystemBarIcons()

    var showLoader by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1200)
        showLoader = true
    }

    val pulse by rememberInfiniteTransition(label = "splashPulse").animateFloat(
        initialValue = 1f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse",
    )
    val logo = remember { splashLogoPath() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashNavy),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            Modifier
                .size(288.dp)
                .graphicsLayer {
                    val scale = if (showLoader) pulse else 1f
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            val unit = size.width / 288f
            withTransform({
                translate(left = 23.7f * unit, top = 22.7f * unit)
                scale(scaleX = 0.192f * unit, scaleY = 0.192f * unit, pivot = Offset.Zero)
            }) {
                drawPath(logo, color = Color.White)
            }
        }

        AnimatedVisibility(
            visible = showLoader,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 72.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(96.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.18f),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Preparando tu curso…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
    }
}


@Composable
private fun ForceLightSystemBarIcons() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            previousStatus?.let { controller.isAppearanceLightStatusBars = it }
            previousNav?.let { controller.isAppearanceLightNavigationBars = it }
        }
    }
}

private fun splashLogoPath(): Path = Path().apply {
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

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}