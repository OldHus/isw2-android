package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.model.TeamError
import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class RenameTeamUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(courseId: String, teamId: String, newName: String): Result<Unit> {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(TeamError.EmptyName)
        }
        return teamRepository.renameTeam(courseId, teamId, trimmedName)
    }
}