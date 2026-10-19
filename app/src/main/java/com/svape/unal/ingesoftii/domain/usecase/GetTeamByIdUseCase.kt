package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Team
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class GetTeamByIdUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, teamId: String): Result<Team> {
        return teamRepository.getTeamById(courseId, teamId)
    }
}