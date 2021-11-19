package com.ravisharma.playbackmusic.fragments.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.prefrences.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistFragmentViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    private val prefManager: PrefManager
) : ViewModel() {

    private var playlists: MutableLiveData<ArrayList<String>> = MutableLiveData()

    fun getAllPlaylists(context: Context): MutableLiveData<ArrayList<String>> {
        viewModelScope.launch {
            playlists.value = prefManager.allPlaylist
        }

        return playlists
    }

    fun getPlaylist(playlistName: String): List<Playlist> {
        return repository.getPlaylist(playlistName)
    }

    fun renamePlaylist(oldPlaylistName: String, newPlaylistName: String) {
        prefManager.renamePlaylist(oldPlaylistName, newPlaylistName);
        viewModelScope.launch(Dispatchers.IO) {
            repository.renamePlaylist(oldPlaylistName, newPlaylistName)
        }
    }

    fun createNewPlaylist(playListName: String) {
        prefManager.createNewPlaylist(playListName)
    }

    fun removePlaylist(playlistName: String) {
        repository.removePlaylist(playlistName)
        prefManager.deletePlaylist(playlistName)
    }
}