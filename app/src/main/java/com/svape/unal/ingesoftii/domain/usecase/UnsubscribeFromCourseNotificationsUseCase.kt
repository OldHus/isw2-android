package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.service.NotificationService

class UnsubscribeFromCourseNotificationsUseCase(private val notificationService: NotificationService) {
    suspend operator fun invoke(courseId: String, role: String): Result<Unit> =
        notificationService.unsubscribeFromCourseTopic(courseId, role)
}