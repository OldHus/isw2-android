package com.svape.unal.ingesoftii.domain.service

interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setUserRole(role: String)
    fun setCourseId(courseId: String)
}