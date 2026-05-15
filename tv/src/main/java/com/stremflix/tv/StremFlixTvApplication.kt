package com.stremflix.tv

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StremFlixTvApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Disable day/night theme changes for Netflix-like consistency
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}