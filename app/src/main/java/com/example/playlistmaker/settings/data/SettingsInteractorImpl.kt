// SettingsInteractorImpl.kt
package com.example.playlistmaker.settings.data

import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.settings.domain.model.ThemeSettings

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun getThemeSettings(): ThemeSettings = repository.getThemeSettings()

    override fun updateThemeSetting(settings: ThemeSettings) =
        repository.updateThemeSetting(settings)
}