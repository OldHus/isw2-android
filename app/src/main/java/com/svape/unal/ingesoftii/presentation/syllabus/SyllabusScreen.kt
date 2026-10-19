package com.svape.unal.ingesoftii.presentation.syllabus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MIN_SCALE = 1f
private const val MAX_SCALE = 4f
private const val DOUBLE_TAP_SCALE = 2.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    courseId: String,
    viewModel: SyllabusViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadSyllabus(courseId)
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val state = uiState) {
            is SyllabusUiState.Loading -> Column(Modifier.fillMaxSize()) {
                AppTopBar(title = "Syllabus", subtitle = "Cargando…")
                LoadingPaper()
            }

            is SyllabusUiState.Success -> {
                val pages = remember(state.pages) { state.pages.map { it.asImageBitmap() } }
                if (pages.isEmpty()) {
                    Column(Modifier.fillMaxSize()) {
                        AppTopBar(title = "Syllabus")
                        NotAvailableView(onRetry = { viewModel.loadSyllabus(courseId) })
                    }
                } else {
                    SyllabusReader(pages = pages)
                }
            }

            is SyllabusUiState.NotAvailable -> Column(Modifier.fillMaxSize()) {
                AppTopBar(title = "Syllabus")
                NotAvailableView(onRetry = { viewModel.loadSyllabus(courseId) })
            }

            is SyllabusUiState.Error -> Column(Modifier.fillMaxSize()) {
                AppTopBar(title = "Syllabus")
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "No pudimos abrir el syllabus",
                        message = state.message,
                        action = {
                            FilledTonalButton(onClick = { viewModel.loadSyllabus(courseId) }) {
                                Icon(
                                    Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingPaper() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(1f / 1.414f)
                .clip(MaterialTheme.shapes.medium)
                .shimmer(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
                repeat(8) { i ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (i % 3 == 2) 0.7f else 1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Preparando el syllabus…", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            Modifier
                .width(160.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun NotAvailableView(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            title = "El syllabus aún no está disponible",
            message = "Tu docente todavía no lo ha subido. Vuelve a revisar más tarde.",
            action = {
                TextButton(onClick = onRetry) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Actualizar")
                }
            },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyllabusReader(pages: List<ImageBitmap>) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var chromeVisible by rememberSaveable { mutableStateOf(true) }
    var zoomedPage by remember { mutableIntStateOf(-1) }
    var showThumbnails by remember { mutableStateOf(false) }
    var showHint by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3_500)
        showHint = false
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = zoomedPage != pagerState.currentPage,
            beyondViewportPageCount = 1,
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            ZoomablePage(
                bitmap = pages[index],
                pageNumber = index + 1,
                isCurrent = pagerState.currentPage == index,
                onZoomChange = { zoomed ->
                    if (zoomed) zoomedPage = index else if (zoomedPage == index) zoomedPage = -1
                },
                onTap = { chromeVisible = !chromeVisible },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 88.dp),
            )
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            AppTopBar(
                title = "Syllabus",
                subtitle = if (pages.size == 1) "1 página" else "${pages.size} páginas",
                actions = {
                    if (pages.size > 1) {
                        IconButton(onClick = { showThumbnails = true }) {
                            Icon(
                                Icons.Outlined.GridView,
                                contentDescription = "Ver todas las páginas"
                            )
                        }
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = showHint,
            enter = fadeIn(),
            exit = fadeOut(tween(600)),
            modifier = Modifier
                .align(Alignment.Center),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.ZoomIn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Pellizca o toca dos veces para ampliar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            PageControls(
                currentPage = pagerState.currentPage,
                pageCount = pages.size,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                onOpenGrid = { showThumbnails = true },
            )
        }
    }

    if (showThumbnails) {
        ThumbnailsSheet(
            pages = pages,
            current = pagerState.currentPage,
            onDismiss = { showThumbnails = false },
            onSelect = { index ->
                showThumbnails = false
                scope.launch { pagerState.animateScrollToPage(index) }
            },
        )
    }
}

@Composable
private fun PageControls(
    currentPage: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenGrid: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = currentPage > 0) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Página anterior"
                )
            }
            Surface(
                onClick = onOpenGrid,
                enabled = pageCount > 1,
                shape = CircleShape,
                color = Color.Transparent,
            ) {
                Text(
                    "Página ${currentPage + 1} de $pageCount",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            IconButton(onClick = onNext, enabled = currentPage < pageCount - 1) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Página siguiente"
                )
            }
        }
    }
}

@Composable
private fun ZoomablePage(
    bitmap: ImageBitmap,
    pageNumber: Int,
    isCurrent: Boolean,
    onZoomChange: (Boolean) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(MIN_SCALE) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun clamp(o: Offset, s: Float): Offset {
        val maxX = (size.width * (s - 1f)) / 2f
        val maxY = (size.height * (s - 1f)) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    LaunchedEffect(isCurrent) {
        if (!isCurrent && scale.value > MIN_SCALE) {
            scale.snapTo(MIN_SCALE)
            offset = Offset.Zero
            onZoomChange(false)
        }
    }

    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()

    Box(
        modifier.pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f)),
            modifier = Modifier
                .aspectRatio(ratio)
                .onSizeChanged { size = it }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val isPinch = event.changes.size > 1
                            val isPanWhileZoomed =
                                scale.value > MIN_SCALE && panChange != Offset.Zero

                            if (isPinch || isPanWhileZoomed) {
                                val newScale =
                                    (scale.value * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
                                scope.launch { scale.snapTo(newScale) }
                                offset = if (newScale > MIN_SCALE) clamp(
                                    offset + panChange * newScale,
                                    newScale
                                ) else Offset.Zero
                                onZoomChange(newScale > MIN_SCALE)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { tap ->
                            scope.launch {
                                if (scale.value > MIN_SCALE) {
                                    offset = Offset.Zero
                                    scale.animateTo(
                                        MIN_SCALE,
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                    onZoomChange(false)
                                } else {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    offset = clamp(
                                        (center - tap) * (DOUBLE_TAP_SCALE - 1f),
                                        DOUBLE_TAP_SCALE
                                    )
                                    onZoomChange(true)
                                    scale.animateTo(
                                        DOUBLE_TAP_SCALE,
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                }
                            }
                        },
                    )
                },
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Página $pageNumber del syllabus",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThumbnailsSheet(
    pages: List<ImageBitmap>,
    current: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow,
    ) {
        Text(
            "Todas las páginas",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(pages) { index, page ->
                val selected = index == current
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = { onSelect(index) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        shadowElevation = if (selected) 6.dp else 2.dp,
                        border = BorderStroke(
                            if (selected) 3.dp else 0.5.dp,
                            if (selected) colors.primary else Color.Black.copy(alpha = 0.08f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(page.width.toFloat() / page.height.toFloat()),
                    ) {
                        Image(
                            bitmap = page,
                            contentDescription = "Ir a la página ${index + 1}",
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) colors.primary else colors.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}