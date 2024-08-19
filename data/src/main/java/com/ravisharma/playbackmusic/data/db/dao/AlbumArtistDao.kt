package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.*
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

    @Transaction
    @Query("DELETE FROM ${Constants.Tables.ALBUM_ARTIST_TABLE} WHERE name IN " +
            "(SELECT albumArtist.name as name FROM ${Constants.Tables.ALBUM_ARTIST_TABLE} as albumArtist LEFT JOIN " +
            "${Constants.Tables.SONG_TABLE} as song ON albumArtist.name = song.albumArtist GROUP BY albumArtist.name " +
            "HAVING COUNT(song.location) = 0)")
    suspend fun cleanAlbumArtistTable()

}