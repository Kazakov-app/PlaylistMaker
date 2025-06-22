package com.example.playlistmaker.search

data class TracksResponse(
    val resultCount: Int,
    val results: List<Track>
)