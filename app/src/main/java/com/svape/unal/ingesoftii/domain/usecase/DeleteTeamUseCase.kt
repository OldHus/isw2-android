package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class DeleteTeamUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, teamId: String): Result<Unit> {
        return teamRepository.deleteTeam(courseId, teamId)
    }
}