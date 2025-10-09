package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.models.Track
import com.example.playlistmaker.search.domain.SearchHistoryInteractor
import com.example.playlistmaker.search.domain.SearchState
import com.example.playlistmaker.search.domain.SearchTracksInteractor

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState> = _state

    init {
        showHistory()
    }

    fun searchTracks(query: String) {
        _state.value = SearchState.Loading
        searchTracksInteractor.searchTracks(
            query, object : SearchTracksInteractor.Consumer {
                override fun consume(tracks: List<Track>) {
                    _state.postValue(
                        if (tracks.isEmpty()) SearchState.Empty else SearchState.Content(
                            tracks
                        )
                    )
                }
            })
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
        showHistory()
    }

    fun showHistory() {
        val history = searchHistoryInteractor.getHistory()
        _state.value = if (history.isEmpty()) {
            SearchState.Start
        } else {
            SearchState.History(history)
        }
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        showHistory()
    }

    fun clearSearchResults() {
        showHistory()
    }
}