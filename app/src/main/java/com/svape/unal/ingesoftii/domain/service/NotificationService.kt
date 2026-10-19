package com.svape.unal.ingesoftii.domain.service

interface NotificationService {
    suspend fun subscribeToCourseTopic(courseId: String, role: String): Result<Unit>
    suspend fun unsubscribeFromCourseTopic(courseId: String, role: String): Result<Unit>
}