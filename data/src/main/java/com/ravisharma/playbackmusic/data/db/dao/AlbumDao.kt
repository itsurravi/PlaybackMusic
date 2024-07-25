package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.AlbumWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.data.utils.Constants
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

    @Transaction
    @Query("DELETE FROM ${Constants.Tables.ALBUM_TABLE} WHERE name IN " +
            "(SELECT album.name as name FROM ${Constants.Tables.ALBUM_TABLE} as album LEFT JOIN " +
            "${Constants.Tables.SONG_TABLE} as song ON album.name = song.album GROUP BY album.name " +
            "HAVING COUNT(song.location) = 0)")
    suspend fun cleanAlbumTable()
}