package com.svape.unal.ingesoftii.domain.model

sealed class PostError : Exception() {
    data object EmptyContent : PostError()
    data object InvalidReactionType : PostError()
    data class Unknown(override val message: String) : PostError()
}