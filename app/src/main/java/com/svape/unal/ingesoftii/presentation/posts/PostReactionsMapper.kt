package com.svape.unal.ingesoftii.presentation.posts

import com.svape.unal.ingesoftii.domain.model.PostReaction
import com.svape.unal.ingesoftii.domain.model.ReactionType

fun emojiFor(type: String): String = when (type) {
    ReactionType.LIKE -> "👍"
    ReactionType.LOVE -> "❤️"
    ReactionType.HAHA -> "😄"
    ReactionType.WOW -> "😮"
    ReactionType.SAD -> "😢"
    ReactionType.ANGRY -> "😠"
    else -> "❔"
}

fun buildReactionSummaries(reactions: List<PostReaction>): List<ReactionSummary> {
    return reactions
        .groupBy { it.type }
        .map { (type, group) ->
            ReactionSummary(
                type = type,
                emoji = emojiFor(type),
                count = group.size,
                reactorNames = group.map { it.studentName }
            )
        }
        .sortedByDescending { it.count }
}