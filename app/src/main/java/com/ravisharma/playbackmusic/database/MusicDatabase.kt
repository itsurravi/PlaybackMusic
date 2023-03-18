package com.ravisharma.playbackmusic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ravisharma.playbackmusic.database.dao.new_daos.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.*

@Database(
    entities = [
        Song2::class,
        Album::class,
        Artist::class,
        Playlist2::class,
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
    abstract fun playList2Dao(): PlaylistDao2
}