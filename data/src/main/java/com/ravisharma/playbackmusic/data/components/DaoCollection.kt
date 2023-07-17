package com.ravisharma.playbackmusic.data.components

import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao

data class DaoCollection(
    val songDao: SongDao,
    val albumDao: AlbumDao,
    val artistDao: ArtistDao,
    val albumArtistDao: AlbumArtistDao,
    val composerDao: ComposerDao,
    val lyricistDao: LyricistDao,
    val genreDao: GenreDao,
    val playlistDao: PlaylistDao
)