package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.ArtistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Artist
import com.ravisharma.playbackmusic.data.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllArtists(data: List<Artist>)

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.ARTIST_TABLE} ORDER BY name ASC")
    fun getAllArtistsWithSongs(): Flow<List<ArtistWithSongs>>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.ARTIST_TABLE} WHERE name = :artistName")
    fun getArtistWithSongsByName(artistName: String): Flow<ArtistWithSongs?>

    @Query("DELETE FROM ${Constants.Tables.ARTIST_TABLE}")
    suspend fun deleteAllArtists()

    @Query("SELECT * FROM ${Constants.Tables.ARTIST_TABLE} WHERE name LIKE '%' || :query || '%'")
    suspend fun searchArtists(query: String): List<Artist>

    @Transaction
    @Query("DELETE FROM ${Constants.Tables.ARTIST_TABLE} WHERE name IN " +
            "(SELECT artist.name as name FROM ${Constants.Tables.ARTIST_TABLE} as artist LEFT JOIN " +
            "${Constants.Tables.SONG_TABLE} as song ON artist.name = song.artist GROUP BY artist.name " +
            "HAVING COUNT(song.location) = 0)")
    suspend fun cleanArtistTable()

}