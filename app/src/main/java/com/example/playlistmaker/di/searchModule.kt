package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.search.data.impl.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.impl.TracksRepositoryImpl
import com.example.playlistmaker.search.data.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.network.RetrofitNetworkClient
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.SearchTracksInteractor
import com.example.playlistmaker.search.domain.SearchTracksInteractorImpl
import com.example.playlistmaker.search.domain.TracksRepository
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.util.PreferenceKeys.PREFS_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {

    single<NetworkClient> {
        RetrofitNetworkClient()
    }

    single<TracksRepository> {
        TracksRepositoryImpl(get())
    }

    single<SearchTracksInteractor> {
        SearchTracksInteractorImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPrefs = androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
            gson = get()
        )
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    viewModel {
        SearchViewModel(
            searchTracksInteractor = get(),
            searchHistoryInteractor = get(),
            gson = get()
        )
    }
}