package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class RemoveStudentFromTeamUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(
        courseId: String,
        teamId: String,
        studentUid: String
    ): Result<Unit> {
        return teamRepository.removeStudentFromTeam(courseId, teamId, studentUid)
    }
}