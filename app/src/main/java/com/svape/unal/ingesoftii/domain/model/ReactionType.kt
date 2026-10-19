package com.svape.unal.ingesoftii.domain.model

object ReactionType {
    const val LIKE = "me_gusta"
    const val LOVE = "me_encanta"
    const val HAHA = "jaja"
    const val WOW = "wow"
    const val SAD = "triste"
    const val ANGRY = "enojado"

    val ALL = listOf(LIKE, LOVE, HAHA, WOW, SAD, ANGRY)
}