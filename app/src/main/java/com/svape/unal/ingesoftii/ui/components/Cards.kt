package com.svape.unal.ingesoftii.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiTheme
import com.svape.unal.ingesoftii.ui.theme.pressScale

@Composable
fun IconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "Ver detalle",
    accent: Color = MaterialTheme.colorScheme.primary,
    badgeCount: Int = 0,
) {
    val ext = IngesoftiiTheme.extendedColors
    val interaction = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = modifier.pressScale(interaction),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = ext.card),
        border = BorderStroke(1.dp, ext.cardBorder),
        interactionSource = interaction,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.35f)))),
        )
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp)) {
            Box {
                IconBubble(icon)
                if (badgeCount > 0) {
                    Badge(Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)) {
                        Text(if (badgeCount > 99) "99+" else badgeCount.toString())
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
fun HeroCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    metricValue: String? = null,
    metricLabel: String? = null,
    actionLabel: String = "Ver detalle",
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = modifier.pressScale(interaction, 0.98f),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = colors.onPrimary
        ),
        interactionSource = interaction,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 148.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.primary,
                            lerp(colors.primary, colors.onPrimaryContainer, 0.45f)
                        )
                    )
                ),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.onPrimary.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 28.dp, y = 12.dp)
                    .graphicsLayer { rotationZ = -12f },
            )
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(
                        icon,
                        container = colors.onPrimary.copy(alpha = 0.16f),
                        content = colors.onPrimary,
                        size = 36.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(12.dp))
                if (metricValue != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(metricValue, style = MaterialTheme.typography.displaySmall)
                        if (metricLabel != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                metricLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .alpha(0.85f),
                            )
                        }
                    }
                } else {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(0.9f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = CircleShape,
                    color = colors.onPrimary.copy(alpha = 0.16f),
                    contentColor = colors.onPrimary
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

data class ListEntry(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val supporting: String? = null,
    val comingSoon: Boolean = false,
    val badgeCount: Int = 0,
)

@Composable
fun SegmentedList(
    entries: List<ListEntry>,
    onClick: (ListEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        entries.forEachIndexed { index, entry ->
            SegmentedRow(entry, segmentShape(index, entries.size), onClick = { onClick(entry) })
        }
    }
}

private fun segmentShape(index: Int, count: Int): RoundedCornerShape {
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

@Composable
private fun SegmentedRow(entry: ListEntry, shape: RoundedCornerShape, onClick: () -> Unit) {
    val ext = IngesoftiiTheme.extendedColors
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = !entry.comingSoon,
        shape = shape,
        color = ext.card,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .alpha(if (entry.comingSoon) 0.6f else 1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBubble(entry.icon, size = 38.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface
                )
                if (entry.supporting != null) {
                    Text(
                        entry.supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            when {
                entry.comingSoon -> StatusPill("Próximamente")
                entry.badgeCount > 0 -> Badge { Text(entry.badgeCount.toString()) }
                else -> Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    null,
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = modifier) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        action?.invoke()
    }
}