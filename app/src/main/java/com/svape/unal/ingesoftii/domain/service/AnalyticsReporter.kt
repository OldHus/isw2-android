package com.svape.unal.ingesoftii.domain.service

interface AnalyticsReporter {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun logScreenView(screenName: String)
    fun setUserRole(role: String)
    fun setCourseId(courseId: String)
}