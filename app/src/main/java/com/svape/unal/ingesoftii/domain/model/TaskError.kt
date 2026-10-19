package com.svape.unal.ingesoftii.domain.model

sealed class TaskError : Exception() {
    data object TaskNotFound : TaskError()
    data object EmptyTitle : TaskError()
    data object NotAuthenticated : TaskError()
    data class InvalidTransition(val from: TaskColumn, val to: TaskColumn) : TaskError()
    data class Unknown(override val message: String) : TaskError()
}