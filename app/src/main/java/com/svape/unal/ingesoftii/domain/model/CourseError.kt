package com.svape.unal.ingesoftii.domain.model

sealed class CourseError : Exception() {
    data class Unknown(override val message: String) : CourseError()
}