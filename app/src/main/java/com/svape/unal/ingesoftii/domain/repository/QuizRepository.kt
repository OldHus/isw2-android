package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.QuizAnswer
import com.svape.unal.ingesoftii.domain.model.QuizQuestion
import com.svape.unal.ingesoftii.domain.model.QuizSession
import kotlinx.coroutines.flow.Flow

interface QuizRepository {

    // Teacher

    suspend fun getQuestionBank(courseId: String): Result<List<QuizQuestion>>

    suspend fun createQuestion(
        courseId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ): Result<QuizQuestion>

    suspend fun updateQuestion(
        courseId: String,
        questionId: String,
        text: String,
        options: List<String>,
        correctOptionIndex: Int
    ): Result<Unit>

    suspend fun deleteQuestion(courseId: String, questionId: String): Result<Unit>

    suspend fun launchQuestion(
        courseId: String,
        question: QuizQuestion,
        durationSeconds: Int
    ): Result<QuizSession>

    suspend fun closeSession(courseId: String, sessionId: String): Result<Unit>

    // Shared

    fun observeLatestSession(courseId: String, withAnswerKey: Boolean = false): Flow<QuizSession?>

    fun observeSessionAnswers(courseId: String, sessionId: String): Flow<List<QuizAnswer>>

    // Student

    suspend fun submitAnswer(courseId: String, session: QuizSession, selectedOptionIndex: Int): Result<Unit>

    fun observeMyAnswer(courseId: String, sessionId: String): Flow<QuizAnswer?>
}
