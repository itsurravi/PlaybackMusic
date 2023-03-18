package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.*
import com.ravisharma.playbackmusic.database.model.new_data.PlaylistWithSongCount
import com.ravisharma.playbackmusic.database.model.new_data.embedded.PlaylistWithSongs
import com.ravisharma.playbackmusic.database.model.new_data.tables.Playlist2
import com.ravisharma.playbackmusic.database.model.new_data.tables.PlaylistExceptId
import com.ravisharma.playbackmusic.database.model.new_data.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao2 {

    @Insert(entity = Playlist2::class)
    suspend fun insertPlaylist(playlist: PlaylistExceptId): Long

    @Delete(entity = Playlist2::class)
    suspend fun deletePlaylist(playlist: Playlist2)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(playlistSongCrossRefs: List<PlaylistSongCrossRef>)

    @Delete
    suspend fun deletePlaylistSongCrossRef(playlistSongCrossRef: PlaylistSongCrossRef)

    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE}")
    fun getAllPlaylists(): Flow<List<Playlist2>>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistName LIKE '%' || :query || '%'")
    suspend fun searchPlaylists(query: String): List<Playlist2>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} NATURAL LEFT JOIN " +
            "(SELECT playlistId, COUNT(*) AS count FROM " +
            "${Constants.Tables.PLAYLIST_SONG_CROSS_REF_TABLE} GROUP BY playlistId)")
    fun getAllPlaylistWithSongCount(): Flow<List<PlaylistWithSongCount>>

}