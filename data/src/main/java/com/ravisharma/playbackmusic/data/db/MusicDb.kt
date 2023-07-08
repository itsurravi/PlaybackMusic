package com.ravisharma.playbackmusic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao
import com.ravisharma.playbackmusic.data.db.model.tables.*

@Database(
    entities = [
        Song::class,
        Album::class,
        Artist::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        Genre::class,
        AlbumArtist::class,
        Composer::class,
        Lyricist::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumArtistDao(): AlbumArtistDao
    abstract fun composerDao(): ComposerDao
    abstract fun lyricistDao(): LyricistDao
    abstract fun genreDao(): GenreDao
    abstract fun playList2Dao(): PlaylistDao
}