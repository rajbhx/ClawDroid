package com.clawdroid.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClawdroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ClawdroidApp
            private set
    }
}
