package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.Team
import com.svape.unal.ingesoftii.domain.model.TeamError
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class CreateTeamUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, name: String): Result<Team> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(TeamError.EmptyName)
        }
        return teamRepository.createTeam(courseId, trimmedName)
    }
}