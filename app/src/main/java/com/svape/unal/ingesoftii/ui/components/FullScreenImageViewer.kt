package com.svape.unal.ingesoftii.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.svape.unal.ingesoftii.ui.theme.Motion
import kotlinx.coroutines.launch

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    contentDescription: String? = null,
    caption: (@Composable () -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(300, easing = Motion.EmphasizedDecelerate)) }

    var targetScale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val scale by animateFloatAsState(targetScale, tween(220), label = "viewerScale")
    var chromeVisible by remember { mutableStateOf(true) }
    val maxCaptionHeight = (LocalConfiguration.current.screenHeightDp * 0.4f).dp

    val dismiss: () -> Unit = {
        scope.launch {
            appear.animateTo(0f, tween(180, easing = Motion.EmphasizedAccelerate))
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = appear.value }
                .background(Color.Black.copy(alpha = 0.94f)),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val enter = 0.85f + 0.15f * appear.value
                        scaleX = scale * enter
                        scaleY = scale * enter
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            targetScale = (targetScale * zoom).coerceIn(1f, 5f)
                            offset = if (targetScale > 1f) offset + pan else Offset.Zero
                        }
                    }
                    .pointerInput(caption != null) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (targetScale > 1f) {
                                    targetScale = 1f
                                    offset = Offset.Zero
                                } else {
                                    targetScale = 2.5f
                                }
                            },
                            onTap = {
                                when {
                                    caption != null -> chromeVisible = !chromeVisible
                                    targetScale <= 1f -> dismiss()
                                }
                            },
                        )
                    },
            )

            if (caption != null) {
                AnimatedVisibility(
                    visible = chromeVisible && targetScale <= 1f,
                    enter = fadeIn(tween(Motion.Medium)) + slideInVertically(tween(Motion.Medium)) { it / 4 },
                    exit = fadeOut(tween(Motion.Short)) + slideOutVertically(tween(Motion.Short)) { it / 4 },
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.25f to Color.Black.copy(alpha = 0.55f),
                                    1f to Color.Black.copy(alpha = 0.9f),
                                ),
                            )
                            .safeDrawingPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 20.dp),
                    ) {
                        Column(
                            Modifier
                                .heightIn(max = maxCaptionHeight)
                                .animateContentSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            CompositionLocalProvider(LocalContentColor provides Color.White) {
                                caption()
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn(tween(Motion.Medium)),
                exit = fadeOut(tween(Motion.Short)),
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                FilledTonalIconButton(
                    onClick = dismiss,
                    modifier = Modifier
                        .safeDrawingPadding()
                        .padding(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                }
            }
        }
    }
}