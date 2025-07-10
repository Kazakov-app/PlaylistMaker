package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = true

    override fun onCreate() {
        super.onCreate()
        val sharedPrefs = getSharedPreferences("APP_PR", MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean("DARK_THEME", true)

        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        val sharedPrefs = getSharedPreferences("APP_PR", MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean("DARK_THEME", darkTheme)
            apply()
        }

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}