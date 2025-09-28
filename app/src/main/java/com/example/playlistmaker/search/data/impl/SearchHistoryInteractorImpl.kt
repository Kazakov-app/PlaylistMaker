package com.example.playlistmaker.search.data.impl

import com.example.playlistmaker.models.Track
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun addTrack(track: Track) = repository.addTrack(track)
    override fun getHistory(): List<Track> = repository.getHistory()
    override fun clearHistory() = repository.clearHistory()
}