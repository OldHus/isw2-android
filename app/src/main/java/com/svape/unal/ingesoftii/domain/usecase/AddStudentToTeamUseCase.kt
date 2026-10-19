package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.repository.TeamRepository

class AddStudentToTeamUseCase(
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(
        courseId: String,
        teamId: String,
        studentUid: String
    ): Result<Unit> {
        return teamRepository.addStudentToTeam(courseId, teamId, studentUid)
    }
}