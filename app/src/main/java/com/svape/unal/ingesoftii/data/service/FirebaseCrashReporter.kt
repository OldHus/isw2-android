package com.svape.unal.ingesoftii.data.service

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.svape.unal.ingesoftii.domain.service.CrashReporter

private const val KEY_USER_ROLE = "user_role"
private const val KEY_COURSE_ID = "course_id"

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) : CrashReporter {

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserRole(role: String) {
        crashlytics.setCustomKey(KEY_USER_ROLE, role)
    }

    override fun setCourseId(courseId: String) {
        crashlytics.setCustomKey(KEY_COURSE_ID, courseId)
    }
}