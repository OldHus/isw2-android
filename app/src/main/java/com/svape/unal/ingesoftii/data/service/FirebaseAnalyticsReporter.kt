package com.svape.unal.ingesoftii.data.service

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.svape.unal.ingesoftii.domain.service.AnalyticsReporter

private const val KEY_USER_ROLE = "user_role"
private const val KEY_COURSE_ID = "course_id"

class FirebaseAnalyticsReporter(
    context: Context
) : AnalyticsReporter {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun setUserRole(role: String) {
        firebaseAnalytics.setUserProperty(KEY_USER_ROLE, role)
    }

    override fun setCourseId(courseId: String) {
        firebaseAnalytics.setUserProperty(KEY_COURSE_ID, courseId)
    }
}