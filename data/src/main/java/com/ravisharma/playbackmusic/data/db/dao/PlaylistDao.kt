package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.embedded.PlaylistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistExceptId
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.data.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(entity = Playlist::class)
    suspend fun insertPlaylist(playlist: PlaylistExceptId): Long

    @Update(entity = Playlist::class)
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("DELETE FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistId = :playlistId")
    suspend fun getPlaylist(playlistId: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(playlistSongCrossRefs: List<PlaylistSongCrossRef>): List<Long>

    @Delete
    suspend fun deletePlaylistSongCrossRef(playlistSongCrossRef: PlaylistSongCrossRef)

    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE}")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} WHERE playlistName LIKE '%' || :query || '%'")
    suspend fun searchPlaylists(query: String): List<Playlist>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.PLAYLIST_TABLE} NATURAL LEFT JOIN " +
            "(SELECT playlistId, COUNT(*) AS count FROM " +
            "${Constants.Tables.PLAYLIST_SONG_CROSS_REF_TABLE} GROUP BY playlistId)")
    fun getAllPlaylistWithSongCount(): Flow<List<PlaylistWithSongCount>>

    @Transaction
    suspend fun transferPlaylistToNewDB(playlist: Map<String, List<String>>) {
        playlist.forEach { (playlistName, songLocations) ->
            if (playlistName.isNotBlank()) {
                val playlistToSave = PlaylistExceptId(
                    playlistName = playlistName.trim(),
                    createdAt = System.currentTimeMillis()
                )
                val playlistId = insertPlaylist(playlistToSave)

                insertPlaylistSongCrossRef(
                    songLocations.map {
                        PlaylistSongCrossRef(
                            playlistId = playlistId,
                            location = it
                        )
                    }
                )
            }
        }
    }
}