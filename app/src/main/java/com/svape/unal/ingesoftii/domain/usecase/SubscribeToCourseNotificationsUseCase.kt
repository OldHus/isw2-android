package com.svape.unal.ingesoftii.domain.usecase

import com.svape.unal.ingesoftii.domain.service.NotificationService

class SubscribeToCourseNotificationsUseCase(private val notificationService: NotificationService) {
    suspend operator fun invoke(courseId: String, role: String): Result<Unit> =
        notificationService.subscribeToCourseTopic(courseId, role)
}