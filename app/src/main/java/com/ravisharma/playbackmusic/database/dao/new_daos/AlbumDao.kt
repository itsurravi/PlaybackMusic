package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.Album
import com.ravisharma.playbackmusic.database.model.new_data.embedded.AlbumWithSongs
import com.ravisharma.playbackmusic.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAlbums(data: List<Album>)

    @Query("SELECT * FROM ${Constants.Tables.ALBUM_TABLE} ORDER BY name ASC")
    fun getAllAlbums(): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.ALBUM_TABLE} WHERE name = :albumName")
    fun getAlbumWithSongsByName(albumName: String): Flow<AlbumWithSongs?>

    @Query("DELETE FROM ${Constants.Tables.ALBUM_TABLE}")
    suspend fun deleteAllAlbums()

    @Query("SELECT * FROM ${Constants.Tables.ALBUM_TABLE} WHERE name LIKE '%' || :query || '%'")
    suspend fun searchAlbums(query: String): List<Album>

}