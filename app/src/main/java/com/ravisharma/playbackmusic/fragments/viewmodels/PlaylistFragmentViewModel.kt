package com.ravisharma.playbackmusic.fragments.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.prefrences.PrefManager
import kotlinx.coroutines.launch

class PlaylistFragmentViewModel : ViewModel() {

    private var playlists: MutableLiveData<ArrayList<String>> = MutableLiveData()

    fun getAllPlaylists(context: Context): MutableLiveData<ArrayList<String>> {
        viewModelScope.launch {
            val p = PrefManager(context)
            playlists.value = p.allPlaylist
        }

        return playlists
    }

    fun getPlaylist(context: Context, playlistName: String): List<Playlist> {
        val repository = PlaylistRepository(context)
        return repository.getPlaylist(playlistName);
    }

    fun removePlaylist(context: Context, playlistName: String) {
        val repository = PlaylistRepository(context)
        repository.removePlaylist(playlistName);
    }
}