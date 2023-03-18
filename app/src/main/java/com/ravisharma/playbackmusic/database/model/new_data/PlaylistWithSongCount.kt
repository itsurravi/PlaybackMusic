package com.ravisharma.playbackmusic.database.model.new_data

data class PlaylistWithSongCount(
    val playlistId: Long,
    val playlistName: String,
    val createdAt: Long,
    val count: Int = 0,
)
