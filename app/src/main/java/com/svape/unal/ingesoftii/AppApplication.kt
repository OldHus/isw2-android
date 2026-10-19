package com.svape.unal.ingesoftii

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.svape.unal.ingesoftii.di.AppContainer

class AppApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}