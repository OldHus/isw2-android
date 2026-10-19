package com.svape.unal.ingesoftii.presentation.grades.teacher

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.domain.model.CourseStudent
import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.presentation.grades.GradeLevel
import com.svape.unal.ingesoftii.presentation.grades.StudentAvatar
import com.svape.unal.ingesoftii.presentation.grades.formatGrade
import com.svape.unal.ingesoftii.presentation.grades.gradeColors
import com.svape.unal.ingesoftii.presentation.grades.gradeLevel
import com.svape.unal.ingesoftii.ui.components.AppTopBar
import com.svape.unal.ingesoftii.ui.components.EmptyState
import com.svape.unal.ingesoftii.ui.components.ErrorState
import com.svape.unal.ingesoftii.ui.components.LoadingList

private val NAME_COLUMN_WIDTH = 150.dp
private val ITEM_COLUMN_WIDTH = 84.dp
private val AVG_COLUMN_WIDTH = 76.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesMatrixScreen(
    courseId: String,
    viewModel: GradesMatrixViewModel,
    onSelectStudent: (studentUid: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(courseId) { viewModel.load(courseId) }

    var lastSuccess by remember { mutableStateOf<GradesMatrixUiState.Success?>(null) }
    LaunchedEffect(uiState) { (uiState as? GradesMatrixUiState.Success)?.let { lastSuccess = it } }
    val content = (uiState as? GradesMatrixUiState.Success) ?: lastSuccess

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Matriz de notas",
                subtitle = content?.let { "${it.students.size} estudiantes · ${it.items.size} ítems" },
            )
        },
    ) { padding ->
        val state = uiState
        when {
            content != null -> {
                if (content.items.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.TableChart,
                        title = "Aún no hay ítems",
                        message = "Crea ítems de calificación para ver la matriz del curso.",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = state is GradesMatrixUiState.Loading,
                        onRefresh = { viewModel.load(courseId) },
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                    ) {
                        Matrix(
                            items = content.items,
                            students = content.students,
                            grades = content.grades,
                            onSelectStudent = onSelectStudent,
                        )
                    }
                }
            }

            state is GradesMatrixUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.load(courseId) },
                modifier = Modifier.padding(padding),
            )

            else -> LoadingList(Modifier.padding(padding), header = false, rowHeight = 52.dp, rows = 10)
        }
    }
}

@Composable
private fun Matrix(
    items: List<GradeItem>,
    students: List<CourseStudent>,
    grades: Map<String, Map<String, Double>>,
    onSelectStudent: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val hScroll = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        Legend(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainer)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCell("Estudiante", Modifier.width(NAME_COLUMN_WIDTH).padding(start = 20.dp), TextAlign.Start)
            Row(Modifier.horizontalScroll(hScroll)) {
                items.forEach { item ->
                    HeaderCell(item.name, Modifier.width(ITEM_COLUMN_WIDTH).padding(horizontal = 6.dp))
                }
                HeaderCell("Prom.", Modifier.width(AVG_COLUMN_WIDTH).padding(horizontal = 6.dp))
            }
        }
        HorizontalDivider(color = colors.outlineVariant)

        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            itemsIndexed(students, key = { _, s -> s.uid }) { index, student ->
                MatrixRow(
                    student = student,
                    items = items,
                    studentGrades = grades[student.uid].orEmpty(),
                    hScroll = hScroll,
                    striped = index % 2 == 1,
                    onClick = { onSelectStudent(student.uid) },
                )
            }
        }
    }
}

@Composable
private fun MatrixRow(
    student: CourseStudent,
    items: List<GradeItem>,
    studentGrades: Map<String, Double>,
    hScroll: ScrollState,
    striped: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val registered = items.mapNotNull { studentGrades[it.id] }
    val average = if (registered.isEmpty()) null else registered.average()

    Row(
        Modifier
            .fillMaxWidth()
            .background(if (striped) colors.surfaceContainerLow else colors.background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .width(NAME_COLUMN_WIDTH)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val displayName = student.name.ifBlank { student.email }
            StudentAvatar(displayName, student.photoUrl, size = 30.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(Modifier.horizontalScroll(hScroll)) {
            items.forEach { item ->
                GradeCell(studentGrades[item.id], Modifier.width(ITEM_COLUMN_WIDTH))
            }
            GradeCell(average, Modifier.width(AVG_COLUMN_WIDTH), bold = true)
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun GradeCell(grade: Double?, modifier: Modifier = Modifier, bold: Boolean = false) {
    val level = gradeLevel(grade)
    val c = gradeColors(level)
    Box(modifier.padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
        if (grade == null) {
            Text("—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        } else {
            Text(
                formatGrade(grade),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.container)
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
                color = c.content,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendDot(GradeLevel.Excellent, "≥ 4.0")
        LegendDot(GradeLevel.Passing, "3.0 – 3.9")
        LegendDot(GradeLevel.Failing, "< 3.0")
    }
}

@Composable
private fun LegendDot(level: GradeLevel, label: String) {
    val c = gradeColors(level)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(c.accent),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}