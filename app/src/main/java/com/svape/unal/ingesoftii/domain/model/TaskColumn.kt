package com.svape.unal.ingesoftii.domain.model

enum class TaskColumn(val firestoreValue: String) {
    PROPUESTAS("propuestas"),
    HACER("hacer"),
    DESARROLLANDO("desarrollando"),
    QA("qa"),
    APROBADA("aprobada");

    companion object {
        fun fromFirestoreValue(value: String): TaskColumn =
            entries.firstOrNull { it.firestoreValue == value } ?: PROPUESTAS
    }
}

object TaskTransitionPolicy {

    private val teamMemberAllowedColumns = setOf(
        TaskColumn.HACER,
        TaskColumn.DESARROLLANDO,
        TaskColumn.QA
    )

    fun canTransition(role: String, from: TaskColumn, to: TaskColumn): Boolean {
        if (from == to) return false
        return when (role) {
            UserRole.TEACHER -> true
            UserRole.STUDENT -> from in teamMemberAllowedColumns && to in teamMemberAllowedColumns
            else -> false
        }
    }
}