package com.ravisharma.playbackmusic.database

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ravisharma.playbackmusic.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.database.dao.SetupDao
import com.ravisharma.playbackmusic.database.model.DatabaseSetup
import com.ravisharma.playbackmusic.model.Playlist

class PlaylistRepository(context: Context?) {
    private val setupDao: SetupDao
    private val playlistDao: PlaylistDao
    private var playlistSong: LiveData<List<Playlist>> = MutableLiveData()

    private val database: PlaylistDatabase

    init {
        database = PlaylistDatabase.getInstance(context)
        setupDao = database.setupDao()
        playlistDao = database.playlistDao()
    }

    val isDatabaseRead: LiveData<DatabaseSetup>
        get() = setupDao.dataSetup()

    //Playlist Operations
    fun getPlaylistSong(playlistName: String): LiveData<List<Playlist>> {
        playlistSong = playlistDao.getPlaylistSong(playlistName)
        return playlistSong
    }

    fun getPlaylist(playlistName: String): List<Playlist> {
        return playlistDao.getPlaylist(playlistName)
    }

    val allPlaylistSongs: List<Playlist>
        get() = playlistDao.allPlaylistSongs

    fun isSongExist(playlistName: String, songId: Long): Long {
        return playlistDao.isSongExist(playlistName, songId)
    }

    fun addSong(playlist: Playlist?) {
        AddSong().execute(playlist)
    }

    fun removeSong(songId: Long) {
        RemoveSong(null, songId).execute()
    }

    fun removeSong(playlistName: String?, songId: Long) {
        RemoveSong(playlistName, songId).execute()
    }

    fun removePlaylist(playlistName: String) {
        RemovePlaylist().execute(playlistName)
    }

    private inner class AddSong : AsyncTask<Playlist, Void?, Void?>() {
        protected override fun doInBackground(vararg playlists: Playlist): Void? {
            playlistDao.addSong(playlists[0])
            return null
        }
    }

    private inner class RemoveSong(var playlistName: String?, var songId: Long) : AsyncTask<Void, Void?, Void?>() {
        protected override fun doInBackground(vararg voids: Void): Void? {
            if (playlistName == null) {
                playlistDao.removeSong(songId)
            } else {
                playlistDao.removeSong(playlistName!!, songId)
            }
            return null
        }

    }

    private inner class RemovePlaylist : AsyncTask<String, Void?, Void?>() {
        protected override fun doInBackground(vararg playlist: String): Void? {
            playlistDao.removePlaylist(playlist[0])
            return null
        }
    }
}