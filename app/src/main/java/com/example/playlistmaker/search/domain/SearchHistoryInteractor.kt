package com.example.playlistmaker.search.domain

import com.example.playlistmaker.models.Track

interface SearchHistoryInteractor {
    fun addTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}