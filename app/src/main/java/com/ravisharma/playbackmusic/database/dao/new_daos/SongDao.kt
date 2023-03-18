package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2
import com.ravisharma.playbackmusic.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllSongs(data: List<Song2>)

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song2>>

    @Update
    suspend fun updateSong(song: Song2)

    @Delete
    suspend fun deleteSong(song: Song2)

    @Query("DELETE FROM ${Constants.Tables.SONG_TABLE}")
    suspend fun deleteAllSongs()

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} WHERE title LIKE '%' || :query || '%' OR " +
            "artist LIKE '%' || :query || '%' OR " +
            "albumArtist LIKE '%' || :query || '%' OR " +
            "composer LIKE '%' || :query || '%' OR " +
            "genre LIKE '%' || :query || '%' OR " +
            "lyricist LIKE '%' || :query || '%'")
    suspend fun searchSongs(query: String): List<Song2>

    @Query("SELECT artist as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.artist")
    fun getAllArtistsWithSongCount(): Flow<List<ArtistWithSongCount>>

    @Query("SELECT albumArtist as name, COUNT(*) as count FROM ${Constants.Tables.SONG_TABLE} GROUP BY " +
            "${Constants.Tables.SONG_TABLE}.albumArtist")
    fun getAllAlbumArtistsWithSongCount(): Flow<List<AlbumArtistWithSongCount>>

    @Query("SELECT * FROM ${Constants.Tables.SONG_TABLE} WHERE favourite = 1")
    fun getAllFavourites(): Flow<List<Song2>>
}