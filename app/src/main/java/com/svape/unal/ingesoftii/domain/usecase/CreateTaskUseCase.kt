package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.model.TaskError
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class CreateTaskUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(
        courseId: String,
        teamId: String,
        title: String,
        description: String
    ): Result<KanbanTask> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(TaskError.EmptyTitle)
        }
        return teamRepository.createTask(courseId, teamId, trimmedTitle, description.trim())
    }
}