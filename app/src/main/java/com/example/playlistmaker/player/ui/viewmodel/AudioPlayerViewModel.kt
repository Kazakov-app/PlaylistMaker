package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.models.Track
import com.example.playlistmaker.player.domain.AudioPlayerInteractor
import com.example.playlistmaker.player.domain.AudioPlayerState

class AudioPlayerViewModel(
    private val interactor: AudioPlayerInteractor,
    private val track: Track
) : ViewModel() {

    private val screenState = MutableLiveData<AudioPlayerState>()

    fun getThemeSettings(): LiveData<AudioPlayerState> = screenState

    init {
        screenState.value = AudioPlayerState.Loading

        interactor.initialize(track)

        interactor.setProgressListener { currentTime ->
            val currentState = screenState.value
            screenState.value = if (currentState is AudioPlayerState.Content) {
                currentState.copy(currentTime = currentTime)
            } else {
                AudioPlayerState.Content(track, currentTime, isPlaying = true)
            }
        }

        interactor.setCompletionListener {
            screenState.value = AudioPlayerState.Content(track, TIME_PLAY_TRACK, isPlaying = false)
        }
        screenState.value = AudioPlayerState.Content(track, TIME_PLAY_TRACK, isPlaying = false)
    }

    fun togglePlayback() {
        interactor.togglePlayback()
        val currentState = screenState.value
        if (currentState is AudioPlayerState.Content) {
            screenState.value = currentState.copy(isPlaying = !currentState.isPlaying)
        }
    }

    fun pausePlayback() {
        interactor.pausePlayback()
        val currentState = screenState.value
        if (currentState is AudioPlayerState.Content) {
            screenState.value = currentState.copy(isPlaying = false)
        }
    }

    fun releasePlayer() {
        interactor.releasePlayer()
    }

    companion object {
        private const val TIME_PLAY_TRACK = "0:30"
        fun getViewModelFactory(
            track: Track,
            interactor: AudioPlayerInteractor
        ): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AudioPlayerViewModel(interactor, track) as T
                }
            }
    }

}