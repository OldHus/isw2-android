package com.svape.unal.ingesoftii.domain.model

data class GradeItem(
    val id: String,
    val name: String,
    val weight: Double,
    val type: String,
    val order: Int
)