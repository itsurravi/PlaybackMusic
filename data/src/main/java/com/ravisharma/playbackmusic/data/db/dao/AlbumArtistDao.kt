package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.AlbumArtistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.AlbumArtist
import com.ravisharma.playbackmusic.data.utils.Constants
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