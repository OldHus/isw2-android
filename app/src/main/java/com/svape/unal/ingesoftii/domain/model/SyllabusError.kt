package com.svape.unal.ingesoftii.domain.model

sealed class SyllabusError : Exception() {
    data object NotAvailable : SyllabusError()
    data class Unknown(override val message: String) : SyllabusError()
}