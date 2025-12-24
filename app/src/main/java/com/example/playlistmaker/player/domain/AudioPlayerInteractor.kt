package com.example.playlistmaker.player.domain

interface AudioPlayerInteractor {
    fun togglePlayback()
    fun pausePlayback()
    fun releasePlayer()
    fun setProgressListener(listener: (String) -> Unit)
    fun setCompletionListener(listener: () -> Unit)
}
