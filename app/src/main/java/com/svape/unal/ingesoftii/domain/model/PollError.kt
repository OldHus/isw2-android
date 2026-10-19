package com.svape.unal.ingesoftii.domain.model

sealed class PollError : Exception() {
    data object EmptyQuestion : PollError()
    data object NotEnoughOptions : PollError()
    data object InvalidExpiration : PollError()
    data object NoActivePoll : PollError()
    data object PollClosed : PollError()
    data object InvalidOption : PollError()
    data class Unknown(override val message: String) : PollError()
}