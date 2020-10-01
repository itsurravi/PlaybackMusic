package com.ravisharma.playbackmusic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.model.LastPlayed
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.TinyDB
import com.ravisharma.playbackmusic.utils.playingSong
import com.ravisharma.playbackmusic.utils.songPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    private var repository: PlaylistRepository? = null
    private var pSong: MutableLiveData<Song> = MutableLiveData()
    private var sPosition: MutableLiveData<Int> = MutableLiveData()

    fun getPlayingSong(): MutableLiveData<Song> {
        pSong = playingSong
        return pSong
    }

    fun getSongPosition(): MutableLiveData<Int> {
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

    /*fun addSongToLastPlayed(context: Context, song: Song) {
        val lastPlayedRepository = LastPlayedRepository(context)
        viewModelScope.launch(Dispatchers.IO) {
            val existId = lastPlayedRepository.checkSongIfExist(song.id)
            if (existId > 0) {
                lastPlayedRepository.deleteSongFromLastPlayed(song.id)
            }
            val lastPlayed = LastPlayed(song)
            lastPlayedRepository.addSongToLastPlayed(lastPlayed)
        }
    }*/

    /*fun addSongToMostPlayed(context: Context, song: Song) {
        val mostPlayedRepository = MostPlayedRepository(context)
        viewModelScope.launch(Dispatchers.IO) {
            val existId = mostPlayedRepository.checkSongIfExist(song.id)
            if (existId > 0) {
                val playedId = mostPlayedRepository.getPlayedId(song.id)
                mostPlayedRepository.updatePlayCount(playedId)
            } else {
                val mostPlayed = MostPlayed(song, 1)
                mostPlayedRepository.addSongToMostPlayed(mostPlayed)
            }
        }
    }*/
}