package com.ravisharma.playbackmusic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.curPlayingSong
import com.ravisharma.playbackmusic.utils.curPlayingSongPosition
import com.ravisharma.playbackmusic.utils.getPlayingListData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    private val tinyDb: TinyDB,
    private val pref: PrefManager
) : ViewModel() {

    private var pSong: MutableLiveData<Song> = MutableLiveData()
    private var sPosition: MutableLiveData<Int> = MutableLiveData()
    private var currentPlayingList = MutableLiveData<ArrayList<Song>?>()

    init {
        currentPlayingList = getPlayingListData()
    }

    fun getPlayingList(): LiveData<ArrayList<Song>?> {
        return currentPlayingList
    }

    fun getPlayingSong(): LiveData<Song> {
        pSong = curPlayingSong
        return pSong
    }

    fun getSongPosition(): LiveData<Int> {
        sPosition = curPlayingSongPosition
        return sPosition
    }

    fun getTinyDbSongs(songs: String): ArrayList<Song> {
        return tinyDb.getListObject(songs, Song::class.java)
    }

    fun saveTinyDbSongs(songsType: String, songList: java.util.ArrayList<Song>) {
        tinyDb.putListObject(songsType, songList)
    }

    fun getPlaylistSong(playlistName: String): LiveData<List<Playlist>> {
        return repository.getPlaylistSong(playlistName)
    }

    fun isSongExist(playlistName: String, songId: Long): Long {
        return repository.isSongExist(playlistName, songId)
    }

    fun removeSong(playlistName: String, songId: Long) {
        return repository.removeSong(playlistName, songId)
    }

    fun addSong(playlist: Playlist) {
        repository.addSong(playlist)
    }

    fun removeSongFromPlaylist() {
        val playlists = repository.allPlaylistSongs
        for ((_, _, s) in playlists) {
            if (!songListByName.value!!.contains(s)) {
                repository.removeSong(s.id)
            }
        }
    }

    fun createNewPlaylist(playListName: String) {
        viewModelScope.launch {
            pref.createNewPlaylist(playListName)
        }
    }
}