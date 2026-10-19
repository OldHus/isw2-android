package com.svape.unal.ingesoftii.domain.model

data class StudentGrades(
    val studentUid: String,
    val grades: Map<String, Double>
)