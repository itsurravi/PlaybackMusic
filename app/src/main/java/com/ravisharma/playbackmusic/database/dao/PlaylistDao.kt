package com.ravisharma.playbackmusic.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.model.Playlist

@Dao
interface PlaylistDao {
    @Insert
    fun addSong(playlist: Playlist)

    @Query("DELETE FROM playlistTable WHERE id=:songId")
    fun removeSong(songId: Long)

    @Query("DELETE FROM playlistTable WHERE playlistName=:playlistName AND id=:songId")
    fun removeSong(playlistName: String, songId: Long)

    @Query("DELETE FROM playlistTable WHERE playlistName=:playlistName")
    fun removePlaylist(playlistName: String)

    @Query("UPDATE playlistTable SET playlistName=:newPlaylistName WHERE playlistName=:oldPlaylistName")
    suspend fun renamePlaylist(oldPlaylistName: String, newPlaylistName: String)

    @get:Query("SELECT * FROM playlistTable")
    val allPlaylistSongs: List<Playlist>

    @Query("SELECT * FROM playlistTable WHERE playlistName=:playlistName")
    fun getPlaylistSong(playlistName: String): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlistTable WHERE playlistName=:playlistName")
    fun getPlaylist(playlistName: String): List<Playlist>

    @Query("SELECT COUNT(*) FROM playlistTable WHERE playlistName=:playlistName AND id=:songId")
    fun isSongExist(playlistName: String, songId: Long): Long
}