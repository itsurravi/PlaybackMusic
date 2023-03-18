package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.ArtistWithSongCount
import com.ravisharma.playbackmusic.database.model.new_data.tables.Artist
import com.ravisharma.playbackmusic.database.model.new_data.embedded.ArtistWithSongs
import com.ravisharma.playbackmusic.utils.Constants
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
    @Query("SELECT ${Constants.Tables.ARTIST_TABLE}.name as artistName, COUNT(*) as count " +
            "FROM ${Constants.Tables.ARTIST_TABLE} JOIN ${Constants.Tables.SONG_TABLE} ON " +
            "${Constants.Tables.ARTIST_TABLE}.name = ${Constants.Tables.SONG_TABLE}.artist " +
            "GROUP BY ${Constants.Tables.ARTIST_TABLE}.name")
    fun getAllArtistsWithSongCount(): Flow<List<ArtistWithSongCount>>

}