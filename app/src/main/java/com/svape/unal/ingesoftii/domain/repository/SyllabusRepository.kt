package com.svape.unal.ingesoftii.domain.repository

import java.io.File

interface SyllabusRepository {
    suspend fun downloadSyllabus(courseId: String): Result<File>
}