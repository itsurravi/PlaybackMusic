package com.ravisharma.playbackmusic.activities.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import kotlinx.coroutines.launch

class AddToPlaylistViewModel : ViewModel() {

    private var playlists: MutableLiveData<ArrayList<String>> = MutableLiveData()

    fun getAllPlaylists(context: Context): MutableLiveData<ArrayList<String>> {
        viewModelScope.launch {
            val p = PrefManager(context)
            playlists.value = p.allPlaylist
        }

        return playlists
    }

    fun addToPlaylist(context: Context, playListName: String, song: Song) {
        val repository = PlaylistRepository(context)
        viewModelScope.launch {
            val exist: Long = repository.isSongExist(playListName, song.id)
            if (exist > 0) {
                Toast.makeText(context, "Already Present", Toast.LENGTH_SHORT).show()
            } else {
                val p = Playlist(0, playListName, song)
                repository.addSong(p)
                Toast.makeText(context, "Song Added To Playlist", Toast.LENGTH_SHORT).show()
            }
        }
    }
}