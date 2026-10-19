package com.svape.unal.ingesoftii.presentation.posts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.svape.unal.ingesoftii.domain.model.Post
import com.svape.unal.ingesoftii.domain.model.ReactionType
import com.svape.unal.ingesoftii.ui.components.FullScreenImageViewer
import com.svape.unal.ingesoftii.ui.components.ProfileAvatar
import com.svape.unal.ingesoftii.ui.components.shimmer
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.Motion
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val COLLAPSED_LINES = 6

fun reactionLabel(type: String?): String = when (type) {
    ReactionType.LIKE -> "Me gusta"
    ReactionType.LOVE -> "Me encanta"
    ReactionType.HAHA -> "Me divierte"
    ReactionType.WOW -> "Me asombra"
    ReactionType.SAD -> "Me entristece"
    ReactionType.ANGRY -> "Me enoja"
    else -> "Reaccionar"
}

@Composable
fun PostCard(
    post: Post,
    authorLabel: String,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val ext = IngesoftiiTheme.extendedColors
    val colors = MaterialTheme.colorScheme
    var expanded by rememberSaveable(post.id) { mutableStateOf(false) }
    var overflowing by remember(post.id, post.text) { mutableStateOf(false) }
    var showImage by remember { mutableStateOf(false) }
    val imageUrl = post.imageUrl?.takeIf { it.isNotBlank() }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = ext.card,
        border = BorderStroke(1.dp, ext.cardBorder),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(top = 14.dp, bottom = 6.dp)) {
            Row(
                Modifier.padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Campaign,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(authorLabel, style = MaterialTheme.typography.titleSmall)
                    PostTimestamp(createdAt = post.createdAt, editedAt = post.editedAt)
                }
                if (headerAction != null) headerAction() else Spacer(Modifier.width(12.dp))
            }

            if (post.text.isNotBlank()) {
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                        .animateContentSize(tween(Motion.Medium)),
                ) {
                    PostText(
                        text = post.text,
                        style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                        maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
                        onOverflowChange = { if (!expanded) overflowing = it },
                    )
                    if (overflowing || expanded) {
                        Text(
                            if (expanded) "Ver menos" else "Ver más",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { expanded = !expanded }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                        )
                    }
                }
            }

            if (imageUrl != null) {
                PostImage(
                    url = imageUrl,
                    onClick = { showImage = true },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                )
            }

            footer()
        }
    }

    if (showImage && imageUrl != null) {
        FullScreenImageViewer(
            imageUrl = imageUrl,
            onDismiss = { showImage = false },
            contentDescription = "Imagen de la publicación",
            caption = { ImageCaption(post = post, authorLabel = authorLabel) },
        )
    }
}

@Composable
private fun ImageCaption(post: Post, authorLabel: String) {
    val meta = buildList {
        add(authorLabel)
        if (post.createdAt > 0L) add(relativeTime(post.createdAt))
        val edited = post.editedAt
        if (edited != null && toMillis(edited) > toMillis(post.createdAt) + 60_000) add("Editado")
    }.joinToString(" · ")

    Text(
        meta,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White.copy(alpha = 0.75f),
    )
    if (post.text.isNotBlank()) {
        Spacer(Modifier.height(6.dp))
        PostText(
            text = post.text,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            linkColor = Color(0xFF8FDDF7),
        )
    }
}


private val esLocale = Locale("es", "CO")

private fun toMillis(value: Long): Long =
    if (value in 1 until 100_000_000_000L) value * 1000 else value

fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val time = toMillis(timestamp)
    val diff = (now - time).coerceAtLeast(0)
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = calendarDaysBetween(time, now)
    val sameYear = Calendar.getInstance().apply { timeInMillis = time }.get(Calendar.YEAR) ==
            Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)

    return when {
        minutes < 1 -> "Justo ahora"
        minutes < 60 -> "hace $minutes min"
        hours < 24 && days == 0 -> "hace $hours h"
        days == 1 -> "Ayer, " + format("h:mm a", time)
        days < 7 -> "hace $days días"
        sameYear -> format("d MMM", time).replace(".", "")
        else -> format("d MMM yyyy", time).replace(".", "")
    }
}

private fun calendarDaysBetween(from: Long, to: Long): Int {
    fun startOfDay(millis: Long) = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return Math.round((startOfDay(to) - startOfDay(from)) / 86_400_000.0).toInt()
}

private fun format(pattern: String, millis: Long): String =
    SimpleDateFormat(pattern, esLocale).format(Date(millis))

private fun fullDate(timestamp: Long): String =
    format("d 'de' MMMM 'de' yyyy, h:mm a", toMillis(timestamp))

@Composable
private fun PostTimestamp(createdAt: Long, editedAt: Long?) {
    val colors = MaterialTheme.colorScheme
    if (createdAt <= 0L) {
        Text(
            "Anuncio para la clase",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        return
    }
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(60_000)
            value = System.currentTimeMillis()
        }
    }
    val edited = editedAt != null && toMillis(editedAt) > toMillis(createdAt) + 60_000
    var showFull by remember { mutableStateOf(false) }
    val label = if (showFull) fullDate(createdAt) else relativeTime(createdAt, now)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClickLabel = "Ver fecha completa") { showFull = !showFull }
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Publicado el ${fullDate(createdAt)}" + if (edited) ", editado" else ""
            },
    ) {
        Icon(
            Icons.Outlined.Schedule,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.width(4.dp))
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn(tween(Motion.Short)) togetherWith fadeOut(tween(Motion.Short)) },
            label = "postTime",
        ) { text ->
            Text(
                text + if (edited) " · Editado" else "",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PostImage(url: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    var ratio by remember(url) { mutableFloatStateOf(4f / 3f) }

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(MaterialTheme.shapes.large)
            .background(colors.surfaceContainerHigh)
            .then(if (loading) Modifier.shimmer() else Modifier)
            .clickable(
                enabled = !failed && !loading,
                onClickLabel = "Ver imagen completa",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Imagen de la publicación",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onSuccess = { state ->
                loading = false
                val size = state.painter.intrinsicSize
                if (size.isSpecified && size.height > 0f) {
                    ratio = (size.width / size.height).coerceIn(0.8f, 1.91f)
                }
            },
            onError = {
                loading = false
                failed = true
            },
        )
        if (failed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.BrokenImage,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant
                )
                Text(
                    "No se pudo cargar la imagen",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}


@Composable
fun ReactionSummaryRow(
    reactions: List<ReactionSummary>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = reactions.sumOf { it.count }
    if (total == 0) return
    val colors = MaterialTheme.colorScheme
    val ext = IngesoftiiTheme.extendedColors
    val top = reactions.take(3)
    val firstName = reactions.firstOrNull()?.reactorNames?.firstOrNull()?.let(::shortName)
    val label = when {
        firstName == null -> if (total == 1) "1 reacción" else "$total reacciones"
        total == 1 -> firstName
        else -> "$firstName y ${total - 1} más"
    }

    Row(
        modifier
            .clip(CircleShape)
            .clickable(onClickLabel = "Ver reacciones", onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier
            .width(24.dp + 16.dp * (top.size - 1))
            .height(24.dp)) {
            top.forEachIndexed { index, summary ->
                Box(
                    Modifier
                        .offset(x = 16.dp * index)
                        .zIndex((top.size - index).toFloat())
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerHighest)
                        .border(1.5.dp, ext.card, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(summary.emoji, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun shortName(name: String): String =
    name.trim().split(Regex("\\s+")).take(2).joinToString(" ")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionsSheet(
    reactions: List<ReactionSummary>,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val total = reactions.sumOf { it.count }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val safeSelected = selected.coerceIn(0, reactions.size)
    val entries: List<Pair<String, String>> = remember(reactions, safeSelected) {
        if (safeSelected == 0) {
            reactions.flatMap { s -> s.reactorNames.map { it to s.emoji } }
                .sortedBy { it.first.lowercase() }
        } else {
            val s = reactions[safeSelected - 1]
            s.reactorNames.sortedBy { it.lowercase() }.map { it to s.emoji }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = colors.surfaceContainerLow,
    ) {
        Text(
            "Reacciones",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))
        PrimaryScrollableTabRow(
            selectedTabIndex = safeSelected,
            edgePadding = 12.dp,
            containerColor = Color.Transparent,
            divider = { HorizontalDivider(color = colors.outlineVariant) },
        ) {
            Tab(
                selected = safeSelected == 0,
                onClick = { selected = 0 },
                text = { Text("Todas · $total") },
            )
            reactions.forEachIndexed { index, summary ->
                Tab(
                    selected = safeSelected == index + 1,
                    onClick = { selected = index + 1 },
                    text = { Text("${summary.emoji} ${summary.count}") },
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        ) {
            itemsIndexed(
                entries,
                key = { i, e -> "$i-${e.first}-${e.second}" }) { _, (name, emoji) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        ProfileAvatar(name = name, photoUrl = null, role = null, size = 42.dp)
                        Text(
                            emoji,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp),
                        )
                    }
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReactionBar(
    myReaction: String?,
    onReact: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    var showPicker by remember { mutableStateOf(false) }
    val active = myReaction != null

    val container by animateColorAsState(
        if (active) colors.primaryContainer else colors.surfaceContainerHighest,
        label = "reactionContainer",
    )
    val content by animateColorAsState(
        if (active) colors.onPrimaryContainer else colors.onSurfaceVariant,
        label = "reactionContent",
    )

    val bounce = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(myReaction) }
    LaunchedEffect(myReaction) {
        if (myReaction != previous) {
            previous = myReaction
            if (myReaction != null) {
                bounce.snapTo(0.4f)
                bounce.animateTo(
                    1f,
                    spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }

    val pickerOffsetY = with(LocalDensity.current) { (-56).dp.roundToPx() }

    Box(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = container,
                contentColor = content,
                modifier = Modifier
                    .clip(CircleShape)
                    .combinedClickable(
                        onClickLabel = if (active) "Quitar reacción" else "Me gusta",
                        onLongClickLabel = "Elegir reacción",
                        onClick = { onReact(myReaction ?: ReactionType.LIKE) },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showPicker = true
                        },
                    ),
            ) {
                Row(
                    Modifier.padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = myReaction,
                        transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                        label = "reactionIcon",
                    ) { reaction ->
                        if (reaction != null) {
                            Text(
                                emojiFor(reaction),
                                fontSize = 18.sp,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = bounce.value
                                    scaleY = bounce.value
                                },
                            )
                        } else {
                            Icon(
                                Icons.Outlined.ThumbUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (active) reactionLabel(myReaction) else "Me gusta",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    Icons.Outlined.AddReaction,
                    contentDescription = "Elegir reacción",
                    tint = colors.onSurfaceVariant
                )
            }
        }

        if (showPicker) {
            Popup(
                alignment = Alignment.BottomStart,
                offset = IntOffset(0, pickerOffsetY),
                onDismissRequest = { showPicker = false },
                properties = PopupProperties(focusable = true),
            ) {
                ReactionPicker(
                    selected = myReaction,
                    onPick = { type ->
                        showPicker = false
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (type != myReaction) onReact(type)
                    },
                )
            }
        }
    }
}

@Composable
private fun ReactionPicker(selected: String?, onPick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = colors.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ReactionType.ALL.forEachIndexed { index, type ->
                val appear = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    delay(index * 35L)
                    appear.animateTo(
                        1f,
                        spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
                    )
                }
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    if (pressed) 1.35f else 1f,
                    spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                    label = "pickPress",
                )
                Box(
                    Modifier
                        .size(46.dp)
                        .graphicsLayer {
                            val s = appear.value * pressScale
                            scaleX = s
                            scaleY = s
                            alpha = appear.value.coerceIn(0f, 1f)
                            translationY = (1f - appear.value) * 24f
                        }
                        .clip(CircleShape)
                        .background(if (type == selected) colors.primaryContainer else Color.Transparent)
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            onClickLabel = reactionLabel(type),
                        ) { onPick(type) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emojiFor(type), fontSize = 28.sp)
                }
            }
        }
    }
}