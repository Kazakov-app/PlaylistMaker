package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.settings.data.SettingsInteractorImpl
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.util.PreferenceKeys
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val settingsModule = module {

    single<SettingsRepository> {
        val sharedPrefs = androidContext().getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
        SettingsRepositoryImpl(sharedPrefs)
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    viewModel { (context: Context) ->
        SettingsViewModel(
            settingsInteractor = get(),
            sharingInteractor = get { parametersOf(context) }
        )
    }
}