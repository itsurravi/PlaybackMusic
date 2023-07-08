package com.ravisharma.playbackmusic.data.db.model

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

data class ComposerWithSongCount(
    override val name: String,
    override val count: Int
) : PersonWithSongCount

data class LyricistWithSongCount(
    override val name: String,
    override val count: Int
) : PersonWithSongCount