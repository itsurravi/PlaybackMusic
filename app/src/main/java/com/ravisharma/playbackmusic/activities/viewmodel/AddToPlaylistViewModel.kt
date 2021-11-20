package com.ravisharma.playbackmusic.activities.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.LinkedHashSet
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    private val pref : PrefManager
) : ViewModel() {

    fun getAllPlaylists(): MutableLiveData<ArrayList<String>> {
        return pref.fetchAllPlayList().map {
            ArrayList(LinkedHashSet(it))
        } as MutableLiveData<ArrayList<String>>
    }

    fun addToPlaylist(context: Context, playListName: String, song: Song) {
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

    fun createNewPlaylist(playListName: String) {
        viewModelScope.launch {
            pref.createNewPlaylist(playListName)
        }
    }
}