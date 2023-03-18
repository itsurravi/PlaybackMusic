package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.AlbumArtist
import com.ravisharma.playbackmusic.database.model.new_data.embedded.AlbumArtistWithSongs
import com.ravisharma.playbackmusic.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumArtistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAlbumArtists(data: List<AlbumArtist>)

    @Query("SELECT * FROM ${Constants.Tables.ALBUM_ARTIST_TABLE} WHERE name LIKE '%' || :query || '%'")
    suspend fun searchAlbumArtists(query: String): List<AlbumArtist>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.ALBUM_ARTIST_TABLE} WHERE name = :name")
    fun getAlbumArtistWithSongs(name: String): Flow<AlbumArtistWithSongs?>

}