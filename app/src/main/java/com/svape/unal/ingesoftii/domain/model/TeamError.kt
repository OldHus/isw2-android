package com.svape.unal.ingesoftii.domain.model

sealed class TeamError : Exception() {
    data object TeamNotFound : TeamError()
    data object CourseNotFound : TeamError()
    data object StudentAlreadyInTeam : TeamError()
    data object StudentNotInCourse : TeamError()
    data object EmptyName : TeamError()
    data class Unknown(override val message: String) : TeamError()
}