package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.KanbanTask
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class GetTeamTasksUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, teamId: String): Result<List<KanbanTask>> {
        return teamRepository.getTeamTasks(courseId, teamId)
    }
}