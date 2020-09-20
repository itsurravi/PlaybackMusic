package com.ravisharma.playbackmusic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ravisharma.playbackmusic.database.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.TinyDB
import com.ravisharma.playbackmusic.utils.playingSong
import com.ravisharma.playbackmusic.utils.songPosition

class MainActivityViewModel : ViewModel() {

    private var repository: PlaylistRepository? = null
    private var pSong: MutableLiveData<Song> = MutableLiveData()
    private var sPosition: MutableLiveData<Int> = MutableLiveData()

    fun getPlayingSong() : MutableLiveData<Song>{
        pSong = playingSong
        return pSong
    }

    fun getSongPosition() : MutableLiveData<Int>{
        sPosition = songPosition
        return sPosition
    }

    fun getShuffleSongs(context: Context): ArrayList<Song> {
        val tinydb = TinyDB(context)
        return tinydb.getListObject(context.getString(R.string.Songs), Song::class.java)
    }

    fun getQueueSongs(context: Context): ArrayList<Song> {
        val tinyDb = TinyDB(context)
        return tinyDb.getListObject(context.getString(R.string.NormalSongs), Song::class.java)
    }

    fun getPlaylistSong(context: Context, playlistName: String): LiveData<List<Playlist>> {
        repository = PlaylistRepository(context)
        return repository!!.getPlaylistSong(playlistName)
    }

    fun saveQueueSongs(context: Context, songList: java.util.ArrayList<Song>) {
        val tinyDb = TinyDB(context)
        tinyDb.putListObject(context.getString(R.string.NormalSongs), songList)
    }

    fun saveShuffleSongs(context: Context, songList: java.util.ArrayList<Song>) {
        val tinyDb = TinyDB(context)
        tinyDb.putListObject(context.getString(R.string.Songs), songList)
    }

    fun isSongExist(context: Context, playlistName: String, songId: Long): Long {
        repository = PlaylistRepository(context)
        return repository!!.isSongExist(playlistName, songId)
    }

    fun removeSong(context: Context, playlistName: String, songId: Long) {
        repository = PlaylistRepository(context)
        return repository!!.removeSong(playlistName, songId)
    }

    fun addSong(context: Context, playlist: Playlist) {
        repository = PlaylistRepository(context)
        repository!!.addSong(playlist)
    }
}