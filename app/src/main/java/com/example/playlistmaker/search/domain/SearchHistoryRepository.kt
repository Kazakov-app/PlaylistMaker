package com.example.playlistmaker.search.domain

import com.example.playlistmaker.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun addTrack(track: Track)
    fun getHistory(): Flow<List<Track>>
    fun clearHistory()
}