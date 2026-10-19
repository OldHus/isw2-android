package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.TaskColumn
import com.svape.unal.ingesoftii.domain.model.TaskError
import com.svape.unal.ingesoftii.domain.model.TaskTransitionPolicy
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class MoveTaskUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(
        courseId: String,
        teamId: String,
        taskId: String,
        role: String,
        from: TaskColumn,
        to: TaskColumn
    ): Result<Unit> {
        if (!TaskTransitionPolicy.canTransition(role, from, to)) {
            return Result.failure(TaskError.InvalidTransition(from, to))
        }
        return teamRepository.moveTask(courseId, teamId, taskId, to)
    }
}