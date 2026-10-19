package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class DeleteTaskUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, teamId: String, taskId: String): Result<Unit> {
        return teamRepository.deleteTask(courseId, teamId, taskId)
    }
}