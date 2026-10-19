package com.svape.unal.ingesoftii.navigation

enum class DeepLinkTarget(val typeKey: String) {
    POST("post"),
    POLL("poll"),
    QUIZ("quiz");

    companion object {
        fun fromTypeKey(value: String?): DeepLinkTarget? = entries.find { it.typeKey == value }
    }
}