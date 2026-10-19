package com.svape.unal.ingesoftii.domain.repository

import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.Team
import com.svape.unal.ingesoftii.domain.model.TaskColumn

interface TeamRepository {

    // Teams

    suspend fun createTeam(courseId: String, name: String): Result<Team>
    suspend fun getCourseTeams(courseId: String): Result<List<Team>>
    suspend fun getTeamById(courseId: String, teamId: String): Result<Team>
    suspend fun addStudentToTeam(courseId: String, teamId: String, studentUid: String): Result<Unit>
    suspend fun removeStudentFromTeam(
        courseId: String,
        teamId: String,
        studentUid: String
    ): Result<Unit>

    suspend fun getMyTeam(courseId: String): Result<Team?>

    // Tasks

    suspend fun getTeamTasks(courseId: String, teamId: String): Result<List<KanbanTask>>

    suspend fun createTask(
        courseId: String,
        teamId: String,
        title: String,
        description: String
    ): Result<KanbanTask>

    suspend fun moveTask(
        courseId: String,
        teamId: String,
        taskId: String,
        newColumn: TaskColumn
    ): Result<Unit>

    suspend fun deleteTask(courseId: String, teamId: String, taskId: String): Result<Unit>

    suspend fun renameTeam(courseId: String, teamId: String, newName: String): Result<Unit>

    suspend fun deleteTeam(courseId: String, teamId: String): Result<Unit>
}