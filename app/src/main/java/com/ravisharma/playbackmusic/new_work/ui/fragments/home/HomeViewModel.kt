package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.new_work.services.DataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val context: Application,
    private val manager: DataManager,
    private val dataProvider: DataProvider,
    private val exoPlayer: ExoPlayer,
) : ViewModel() {

    val allSongs = dataProvider.getAll.songs()
        .catch { exception ->
            Log.e("allSongs", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val allAlbums = dataProvider.getAll.albums()
        .catch { exception ->
            Log.e("allAlbums", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val allArtists = dataProvider.getAll.artists()
        .catch { exception ->
            Log.e("allArtists", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val personsWithSongCount = dataProvider.getAll.albumArtists()
        .catch { exception ->
            Log.e("personsWithSongCount", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val playlistsWithSongCount = dataProvider.getAll.playlists()
        .catch { exception ->
            Log.e("playlistsWithSongCount", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val currentSong = manager.currentSong

    val queue = manager.queue

    val repeatMode = manager.repeatMode

    fun toggleRepeatMode(){
        manager.updateRepeatMode(repeatMode.value.next())
    }

    private val _currentSongPlaying = MutableStateFlow<Boolean?>(null)
    val currentSongPlaying = _currentSongPlaying.asStateFlow()

    private val exoPlayerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            _currentSongPlaying.update { isPlaying }
        }
    }

    init {
        _currentSongPlaying.update { exoPlayer.isPlaying }
        exoPlayer.addListener(exoPlayerListener)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(exoPlayerListener)
    }

    /**
     * Shuffle the queue and start playing from first song
     */
    fun shufflePlay(songs: List<Song>?) = setQueue(songs?.shuffled(), 0)

    fun onPlaylistCreate(playlistName: String) {
        viewModelScope.launch {
            dataProvider.createPlaylist(playlistName) {
                showToast(it)
            }
        }
    }

    fun deletePlaylist(playlistWithSongCount: PlaylistWithSongCount) {
        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    playlistId = playlistWithSongCount.playlistId,
                    playlistName = playlistWithSongCount.playlistName,
                    createdAt = playlistWithSongCount.createdAt
                )
                dataProvider.deletePlaylist(playlist)
                showToast("Done")
            } catch (e: Exception) {
                Log.e("deletePlaylist","$e")
                showToast("Some error occurred")
            }
        }
    }

    /**
     * Adds a song to the end of queue
     */
    fun addToQueue(song: Song) {
        if (queue.isEmpty()) {
            manager.setQueue(listOf(song), 0)
        } else {
            val result = manager.addToQueue(song)
            if (result) {
                showToast("Added ${song.title} to queue")
            } else {
                showToast("Song already in queue")
            }
        }
    }

    /**
     * Adds a list of songs to the end queue
     */
    fun addToQueue(songs: List<Song>) {
        if (queue.isEmpty()) {
            manager.setQueue(songs, 0)
        } else {
            var result = false
            songs.forEach { result = result or manager.addToQueue(it) }
            if (result) {
                showToast("Done")
            } else {
                showToast("Songs already in queue")
            }
        }
    }

    /**
     * Adds a song to the next of current playing in queue
     */
    fun addNextInQueue(song: Song) {
        if (queue.isEmpty()) {
            showToast("No song in queue")
        } else {
            val result = manager.addNextInQueue(song)
            if (result) {
                showToast("Added ${song.title} to queue")
            }
        }
    }

    /**
     * Create and set a new queue in exoplayer.
     * Old queue is discarded.
     * Playing starts immediately
     * @param songs queue items
     * @param startPlayingFromIndex index of song from which playing should start
     */
    fun setQueue(songs: List<Song>?, startPlayingFromIndex: Int = 0) {
        if (songs == null) return
        manager.setQueue(songs, startPlayingFromIndex)
        showToast("Playing")
    }

    /**
     * Toggle the favourite value of a song
     */
    fun changeFavouriteValue(song: Song? = currentSong.value) {
        if (song == null) return
        val updatedSong = song.copy(favourite = !song.favourite)
        viewModelScope.launch(Dispatchers.IO) {
            manager.updateSong(updatedSong)
        }
    }
}