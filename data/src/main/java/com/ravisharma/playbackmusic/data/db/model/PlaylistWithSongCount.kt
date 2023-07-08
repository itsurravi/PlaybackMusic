package com.ravisharma.playbackmusic.data.db.model

data class PlaylistWithSongCount(
    val playlistId: Long,
    val playlistName: String,
    val createdAt: Long,
    val count: Int = 0,
)
