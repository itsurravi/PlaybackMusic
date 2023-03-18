package com.ravisharma.playbackmusic.database.model.new_data

sealed interface PersonWithSongCount {
    val name: String
    val count: Int
}

data class ArtistWithSongCount(
    override val name: String,
    override val count: Int
) : PersonWithSongCount

data class AlbumArtistWithSongCount(
    override val name: String,
    override val count: Int
) : PersonWithSongCount