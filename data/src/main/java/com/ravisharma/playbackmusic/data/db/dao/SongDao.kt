package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ravisharma.playbackmusic.data.db.model.AlbumArtistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.ComposerWithSongCount
import com.ravisharma.playbackmusic.data.db.model.GenreWithSongCount
import com.ravisharma.playbackmusic.data.db.model.LyricistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllSongs(data: List<Song>)

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE}")
    suspend fun getSongs(): List<Song>

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM ${Constants.Tables.SONG_TABLE}")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} WHERE title LIKE '%' || :query || '%' OR " +
            "artist LIKE '%' || :query || '%' OR " +
            "albumArtist LIKE '%' || :query || '%' OR " +
            "composer LIKE '%' || :query || '%' OR " +
            "genre LIKE '%' || :query || '%' OR " +
            "lyricist LIKE '%' || :query || '%'")
    suspend fun searchSongs(query: String): List<Song>

    @Query("SELECT artist as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.artist")
    fun getAllArtistsWithSongCount(): Flow<List<ArtistWithSongCount>>

    @Query("SELECT albumArtist as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.albumArtist")
    fun getAllAlbumArtistsWithSongCount(): Flow<List<AlbumArtistWithSongCount>>


    @Query("SELECT composer as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.composer")
    fun getAllComposersWithSongCount(): Flow<List<ComposerWithSongCount>>

    @Query("SELECT lyricist as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.lyricist")
    fun getAllLyricistsWithSongCount(): Flow<List<LyricistWithSongCount>>

    @Query("SELECT genre AS genreName, COUNT(*) AS count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.genre")
    fun getAllGenresWithSongCount(): Flow<List<GenreWithSongCount>>

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} WHERE favourite = 1")
    fun getAllFavourites(): Flow<List<Song>>

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} ORDER BY modifiedDate DESC")
    fun getRecentAdded(): Flow<List<Song>>
}