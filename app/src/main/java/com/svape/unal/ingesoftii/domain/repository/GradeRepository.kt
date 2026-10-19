package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.GradeItem
import com.svape.unal.ingesoftii.domain.model.StudentGrades

interface GradeRepository {

    suspend fun getGradeItems(courseId: String): Result<List<GradeItem>>

    suspend fun addGradeItem(
        courseId: String,
        name: String,
        weight: Double,
        type: String
    ): Result<GradeItem>

    suspend fun renameGradeItem(courseId: String, itemId: String, newName: String): Result<Unit>

    suspend fun countStudentsGraded(courseId: String, itemId: String): Result<Int>

    suspend fun deleteGradeItem(courseId: String, itemId: String): Result<Unit>

    suspend fun getMyGrades(courseId: String): Result<StudentGrades>

    suspend fun getStudentGrades(courseId: String, studentUid: String): Result<StudentGrades>

    suspend fun setStudentGrade(
        courseId: String,
        studentUid: String,
        itemId: String,
        grade: Double
    ): Result<Unit>

    suspend fun deleteStudentGrade(
        courseId: String,
        studentUid: String,
        itemId: String
    ): Result<Unit>

    suspend fun getAllStudentsGrades(courseId: String): Result<Map<String, StudentGrades>>
}