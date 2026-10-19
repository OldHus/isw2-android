package com.svape.unal.ingesoftii.domain.model

sealed class GradeError : Exception() {
    data class InvalidGrade(override val message: String) : GradeError()
    data class Unknown(override val message: String) : GradeError()
}