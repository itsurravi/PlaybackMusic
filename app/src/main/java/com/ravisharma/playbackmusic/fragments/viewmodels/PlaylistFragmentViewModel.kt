package com.ravisharma.playbackmusic.fragments.viewmodels

import androidx.lifecycle.*
import com.ravisharma.playbackmusic.data.olddb.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.data.olddb.model.Playlist
import com.ravisharma.playbackmusic.data.olddb.prefrences.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedHashSet
import javax.inject.Inject

@HiltViewModel
class PlaylistFragmentViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    private val prefManager: PrefManager
) : ViewModel() {

    fun getAllPlaylists(): MutableLiveData<ArrayList<String>> {
        return prefManager.fetchAllPlayList().asLiveData().map {
            ArrayList(LinkedHashSet(it))
        } as MutableLiveData<ArrayList<String>>
    }

    fun getPlaylist(playlistName: String): List<Playlist> {
        return repository.getPlaylist(playlistName)
    }

    fun renamePlaylist(oldPlaylistName: String, newPlaylistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefManager.renamePlaylist(oldPlaylistName, newPlaylistName)
            repository.renamePlaylist(oldPlaylistName, newPlaylistName)
        }
    }

    fun createNewPlaylist(playListName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefManager.createNewPlaylist(playListName)
        }
    }

    fun removePlaylist(playlistName: String) {
        repository.removePlaylist(playlistName)

        viewModelScope.launch(Dispatchers.IO) {
            prefManager.deletePlaylist(playlistName)
        }
    }
}