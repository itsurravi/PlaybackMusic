package com.ravisharma.playbackmusic.new_work.services.data

import com.ravisharma.playbackmusic.data.db.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.db.dao.ThumbnailDao
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.embedded.PlaylistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistExceptId
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow


interface PlaylistService {
    val playlists: Flow<List<PlaylistWithSongCount>>

    fun getPlaylistWithSongsById(playlistId: Long): Flow<PlaylistWithSongs?>

    suspend fun createPlaylist(name: String): Boolean
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylist(updatedPlaylist: Playlist)

    suspend fun addSongsToPlaylist(songLocations: List<String>, playlistId: Long): List<Long>
    suspend fun removeSongsFromPlaylist(songLocations: List<String>, playlistId: Long)
}

class PlaylistServiceImpl(
    private val playlistDao: PlaylistDao,
): PlaylistService {
    override val playlists: Flow<List<PlaylistWithSongCount>>
        = playlistDao.getAllPlaylistWithSongCount()

    override fun getPlaylistWithSongsById(playlistId: Long): Flow<PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }

    override suspend fun createPlaylist(name: String): Boolean {
        if (name.isBlank()) return false
        val playlist = PlaylistExceptId(
            playlistName = name.trim(),
            createdAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        return true
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun updatePlaylist(updatedPlaylist: Playlist) {
        playlistDao.updatePlaylist(updatedPlaylist)
    }

    override suspend fun addSongsToPlaylist(songLocations: List<String>, playlistId: Long): List<Long> {
        return playlistDao.insertPlaylistSongCrossRef(
            songLocations.map {
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    location = it
                )
            }
        )
    }

    override suspend fun removeSongsFromPlaylist(songLocations: List<String>, playlistId: Long) {
        songLocations.forEach {
            playlistDao.deletePlaylistSongCrossRef(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    location = it
                )
            )
        }
    }
}