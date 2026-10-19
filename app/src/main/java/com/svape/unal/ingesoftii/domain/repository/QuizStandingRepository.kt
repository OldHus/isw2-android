package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.QuizStandingEntry
import kotlinx.coroutines.flow.Flow

interface QuizStandingRepository {
    fun observeCourseStanding(courseId: String): Flow<List<QuizStandingEntry>>

    suspend fun resetCourseStanding(courseId: String): Result<Unit>
}