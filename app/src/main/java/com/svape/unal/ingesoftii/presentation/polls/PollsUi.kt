package com.svape.unal.ingesoftii.presentation.polls

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.svape.unal.ingesoftii.domain.model.Poll
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val esLocale = Locale("es", "CO")


@Composable
fun rememberPollRemaining(expiresAt: Long, active: Boolean): Long {
    var remaining by remember(expiresAt, active) {
        mutableLongStateOf((expiresAt - System.currentTimeMillis()).coerceAtLeast(0))
    }
    LaunchedEffect(expiresAt, active) {
        while (active && remaining > 0) {
            delay(1_000)
            remaining = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
        }
    }
    return remaining
}

fun formatPollDeadline(millis: Long, now: Long = System.currentTimeMillis()): String {
    fun startOfDay(m: Long) = Calendar.getInstance().apply {
        timeInMillis = m
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(
        Calendar.SECOND,
        0
    ); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val days = ((startOfDay(millis) - startOfDay(now)) / 86_400_000.0).roundToInt()
    val time = SimpleDateFormat("h:mm a", esLocale).format(Date(millis))
    val sameYear = Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR) ==
            Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)
    val day = when {
        days == 0 -> "hoy"
        days == 1 -> "mañana"
        days == -1 -> "ayer"
        days in 2..6 -> SimpleDateFormat("EEE d MMM", esLocale).format(Date(millis))
        sameYear -> SimpleDateFormat("d MMM", esLocale).format(Date(millis))
        else -> SimpleDateFormat("d MMM yyyy", esLocale).format(Date(millis))
    }.replace(".", "")
    return "$day, $time"
}

@Composable
fun PollStatusBadge(active: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val pulse by rememberInfiniteTransition(label = "pollLive").animateFloat(
        1f, 0.25f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dot",
    )
    Surface(
        shape = CircleShape,
        color = if (active) ext.successContainer else colors.surfaceContainerHighest,
        contentColor = if (active) ext.onSuccessContainer else colors.onSurfaceVariant,
        modifier = modifier,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .alpha(if (active) pulse else 1f)
                    .clip(CircleShape)
                    .background(if (active) ext.success else colors.outline),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (active) "Activa" else "Cerrada",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun PollHeaderCard(
    poll: Poll,
    remainingMillis: Long,
    totalVotes: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val urgencyColor by animateColorAsState(
        when {
            remainingMillis <= 5 * 60_000L -> colors.error
            remainingMillis <= 60 * 60_000L -> ext.warning
            else -> colors.primary
        },
        label = "urgency",
    )
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PollStatusBadge(active = poll.isActive)
                Spacer(Modifier.weight(1f))
                Text(
                    if (totalVotes == 1) "1 voto" else "$totalVotes votos",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(poll.question, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    (if (poll.isActive) "Cierra " else "Cerró ") + formatPollDeadline(poll.expiresAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (poll.isActive && remainingMillis > 0) {
                    Surface(shape = CircleShape, color = urgencyColor.copy(alpha = 0.14f)) {
                        Text(
                            formatRemaining(remainingMillis),
                            style = MaterialTheme.typography.labelMedium,
                            color = urgencyColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PollOptionRow(
    result: PollOptionResult,
    totalVotes: Int,
    isMine: Boolean,
    isLeader: Boolean,
    closed: Boolean,
    showResults: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onShowVoters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val votes = result.voterNames.size
    val fraction = if (totalVotes == 0) 0f else votes.toFloat() / totalVotes
    val animatedFraction by animateFloatAsState(
        if (showResults) fraction else 0f,
        spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow),
        label = "pollBar",
    )
    val percent by animateIntAsState(
        (fraction * 100).roundToInt(),
        tween(600),
        label = "pollPercent"
    )
    val borderColor by animateColorAsState(
        if (isMine) colors.primary else ext.cardBorder,
        label = "pollBorder"
    )
    val barColor = when {
        isMine -> colors.primary
        closed && isLeader -> ext.success
        else -> colors.secondary.copy(alpha = 0.7f)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = if (isMine) colors.primaryContainer.copy(alpha = 0.35f) else ext.card,
        border = BorderStroke(if (isMine) 2.dp else 1.dp, borderColor),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = isMine,
                        transitionSpec = {
                            (scaleIn(spring(dampingRatio = 0.45f)) + fadeIn()) togetherWith (scaleOut() + fadeOut())
                        },
                        label = "pollCheck",
                    ) { mine ->
                        Icon(
                            if (mine) Icons.Rounded.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (mine) "Tu voto" else null,
                            tint = if (mine) colors.primary else colors.outline,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    result.optionText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isMine || (closed && isLeader)) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                if (closed && isLeader && votes > 0) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = "Más votada",
                        tint = Color(0xFFE8B931),
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(20.dp),
                    )
                }
                if (showResults) {
                    Text(
                        "$percent%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = showResults,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHighest),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(animatedFraction)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(barColor),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (votes == 1) "1 voto" else "$votes votos",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        if (votes > 0) {
                            VoterAvatars(result.voterNames, onClick = onShowVoters)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoterAvatars(names: List<String>, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shown = names.take(4)
    val extra = names.size - shown.size
    val size = 24.dp
    val step = 16.dp
    Row(
        Modifier
            .clip(CircleShape)
            .clickable(onClickLabel = "Ver quién votó", onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .width(size + step * (shown.size - 1))
                .height(size)
        ) {
            shown.forEachIndexed { i, name ->
                ProfileAvatar(
                    name = name,
                    photoUrl = null,
                    role = null,
                    size = size,
                    modifier = Modifier
                        .offset(x = step * i)
                        .zIndex((shown.size - i).toFloat())
                        .border(1.5.dp, IngesoftiiTheme.extendedColors.card, CircleShape),
                )
            }
        }
        if (extra > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                "+$extra",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotersSheet(
    results: List<PollOptionResult>,
    initialOption: Int,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var selected by rememberSaveable {
        mutableIntStateOf(
            initialOption.coerceIn(
                0,
                (results.size - 1).coerceAtLeast(0)
            )
        )
    }
    val safeSelected = selected.coerceIn(0, (results.size - 1).coerceAtLeast(0))
    val names = remember(results, safeSelected) {
        results.getOrNull(safeSelected)?.voterNames.orEmpty().sortedBy { it.lowercase() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.surfaceContainerLow,
    ) {
        Text(
            "Quién votó",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (results.isNotEmpty()) {
            PrimaryScrollableTabRow(
                selectedTabIndex = safeSelected,
                edgePadding = 12.dp,
                containerColor = Color.Transparent,
                divider = { HorizontalDivider(color = colors.outlineVariant) },
            ) {
                results.forEachIndexed { index, result ->
                    Tab(
                        selected = safeSelected == index,
                        onClick = { selected = index },
                        text = {
                            Text(
                                "${result.optionText.take(18)}${if (result.optionText.length > 18) "…" else ""} · ${result.voterNames.size}",
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        ) {
            if (names.isEmpty()) {
                item {
                    Text(
                        "Nadie ha votado por esta opción.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
            itemsIndexed(names, key = { i, n -> "$i-$n" }) { _, name ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProfileAvatar(name = name, photoUrl = null, role = null, size = 40.dp)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        name.ifBlank { "Estudiante" },
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}


fun leadingOptionIndex(results: List<PollOptionResult>): Int {
    val max = results.maxOfOrNull { it.voterNames.size } ?: 0
    return if (max == 0) -1 else results.indexOfFirst { it.voterNames.size == max }
}