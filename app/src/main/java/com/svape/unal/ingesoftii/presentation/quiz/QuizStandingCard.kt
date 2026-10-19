package com.svape.unal.ingesoftii.presentation.quiz

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme

private const val STANDING_PREVIEW_COUNT = 5

private data class RankedStanding(val entry: QuizStandingEntry, val rank: Int)

@Composable
fun QuizStandingCard(
    standing: List<QuizStandingEntry>,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    var showAll by rememberSaveable { mutableStateOf(false) }

    val ranked = remember(standing) {
        standing.map { entry ->
            RankedStanding(entry, standing.indexOfFirst { it.totalScore == entry.totalScore } + 1)
        }
    }
    val canCollapse = ranked.size > STANDING_PREVIEW_COUNT
    val visible = if (canCollapse && !showAll) {
        ranked.filterIndexed { index, row -> index < STANDING_PREVIEW_COUNT || row.entry.isCurrentUser }
    } else {
        ranked
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .animateContentSize()
                .padding(vertical = 18.dp),
        ) {
            Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Leaderboard, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ranking acumulado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (standing.size == 1) "1 estudiante" else "${standing.size} estudiantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                action?.invoke()
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 10.dp)) {
                visible.forEach { row ->
                    key(row.entry.studentUid) { StandingRow(row) }
                }
            }
            if (canCollapse) {
                TextButton(
                    onClick = { showAll = !showAll },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(if (showAll) "Ver menos" else "Ver los ${standing.size} estudiantes")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (showAll) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingRow(row: RankedStanding) {
    val colors = MaterialTheme.colorScheme
    val entry = row.entry
    val mine = entry.isCurrentUser
    val animatedScore by animateIntAsState(entry.totalScore, tween(700), label = "standingScore")
    val medal = if (entry.totalScore > 0) {
        when (row.rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> null
        }
    } else {
        null
    }
    val primaryText = if (mine) colors.onPrimaryContainer else colors.onSurface
    val secondaryText = if (mine) colors.onPrimaryContainer else colors.onSurfaceVariant

    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (mine) colors.primaryContainer else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(28.dp), contentAlignment = Alignment.Center) {
            if (medal != null) {
                Text(medal, fontSize = 18.sp)
            } else {
                Text("${row.rank}", style = MaterialTheme.typography.titleSmall, color = secondaryText)
            }
        }
        Spacer(Modifier.width(8.dp))
        ProfileAvatar(name = entry.studentName, photoUrl = null, role = null, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            entry.studentName.ifBlank { "Estudiante" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (mine) FontWeight.SemiBold else FontWeight.Normal,
            color = primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (mine) {
            Spacer(Modifier.width(8.dp))
            Surface(shape = CircleShape, color = colors.surface.copy(alpha = 0.7f)) {
                Text(
                    "Tú",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "$animatedScore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (entry.totalScore > 0) primaryText else secondaryText,
        )
        Text(" pts", style = MaterialTheme.typography.labelMedium, color = secondaryText)
    }
}
