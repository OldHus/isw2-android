package com.svape.unal.ingesoftii.presentation.grades.student

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.presentation.grades.GradePill
import com.svape.unal.ingesoftii.presentation.grades.GradeRing
import com.svape.unal.ingesoftii.presentation.grades.formatGrade
import com.svape.unal.ingesoftii.presentation.grades.gradeLevel
import com.svape.unal.ingesoftii.ui.components.BackButton
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ErrorBanner
import com.svape.unal.ingesoftii.ui.components.ErrorState
import com.svape.unal.ingesoftii.ui.components.LoadingList
import com.svape.unal.ingesoftii.ui.components.SectionHeader
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import com.svape.unal.ingesoftii.ui.theme.StaggeredEnter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentGradesScreen(courseId: String, viewModel: StudentGradesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(courseId) { viewModel.loadGrades(courseId) }

    var lastSuccess by remember { mutableStateOf<StudentGradesUiState.Success?>(null) }
    LaunchedEffect(uiState) { (uiState as? StudentGradesUiState.Success)?.let { lastSuccess = it } }
    val content = (uiState as? StudentGradesUiState.Success) ?: lastSuccess

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        topBar = {
            LargeTopAppBar(
                title = { Text("Mis notas") },
                navigationIcon = { BackButton() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        val state = uiState
        when {
            content != null -> PullToRefreshBox(
                isRefreshing = state is StudentGradesUiState.Loading,
                onRefresh = { viewModel.loadGrades(courseId) },
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                GradesList(
                    items = content.items,
                    grades = content.grades,
                    refreshError = (state as? StudentGradesUiState.Error)?.message,
                )
            }

            state is StudentGradesUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.loadGrades(courseId) },
                modifier = Modifier.padding(padding),
            )

            else -> LoadingList(Modifier.padding(padding))
        }
    }
}

@Composable
private fun GradesList(
    items: List<GradeItem>,
    grades: Map<String, Double>,
    refreshError: String?,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (refreshError != null) {
            item { ErrorBanner(refreshError) }
        }
        item(key = "summary") {
            StaggeredEnter(index = 0) { SummaryCard(items, grades) }
        }
        item(key = "header") {
            StaggeredEnter(index = 1) { SectionHeader("Detalle por ítem") }
        }
        if (items.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    icon = Icons.Outlined.School,
                    title = "Aún no hay ítems",
                    message = "Cuando tu docente cree las evaluaciones del curso, las verás aquí.",
                )
            }
        } else {
            items(items, key = { it.id }) { item ->
                GradeRow(
                    name = item.name,
                    grade = grades[item.id],
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(items: List<GradeItem>, grades: Map<String, Double>) {
    val colors = MaterialTheme.colorScheme
    val graded = items.mapNotNull { grades[it.id] }
    val average = if (graded.isEmpty()) null else graded.average()
    val progressTarget = if (items.isEmpty()) 0f else graded.size.toFloat() / items.size

    val counter = remember { Animatable(0f) }
    LaunchedEffect(average) {
        counter.animateTo(
            (average ?: 0.0).toFloat(),
            tween(1100, easing = Motion.EmphasizedDecelerate)
        )
    }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(1100, easing = Motion.EmphasizedDecelerate),
        label = "gradedProgress",
    )

    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.primary,
        contentColor = colors.onPrimary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            Modifier.background(
                Brush.linearGradient(
                    listOf(
                        colors.primary,
                        androidx.compose.ui.graphics.lerp(
                            colors.primary,
                            colors.onPrimaryContainer,
                            0.45f
                        )
                    ),
                ),
            ),
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Promedio de tus notas",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.alpha(0.85f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (average == null) "—" else formatGrade(counter.value.toDouble()),
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = colors.onPrimary,
                        trackColor = colors.onPrimary.copy(alpha = 0.22f),
                        drawStopIndicator = {},
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${graded.size} de ${items.size} ítems calificados",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.9f),
                    )
                }
                Spacer(Modifier.width(16.dp))
                GradeRing(
                    grade = average,
                    size = 88.dp,
                    strokeWidth = 8.dp,
                    accent = colors.onPrimary,
                    track = colors.onPrimary.copy(alpha = 0.22f),
                    textColor = colors.onPrimary,
                    textStyle = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "Promedio simple de las notas registradas. Tu nota final se calcula según el syllabus.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun GradeRow(name: String, grade: Double?, modifier: Modifier = Modifier) {
    val ext = IngesoftiiTheme.extendedColors
    Surface(
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GradeRing(grade = grade, size = 54.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                GradePill(gradeLevel(grade))
            }
        }
    }
}