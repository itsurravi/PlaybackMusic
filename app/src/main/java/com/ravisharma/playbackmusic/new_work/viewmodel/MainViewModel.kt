package com.ravisharma.playbackmusic.new_work.viewmodel

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.utils.Constants
import com.ravisharma.playbackmusic.new_work.data_proto.QueueState
import com.ravisharma.playbackmusic.new_work.services.PlayerHelper
import com.ravisharma.playbackmusic.new_work.services.data.PlayerService
import com.ravisharma.playbackmusic.new_work.services.data.PlaylistService
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.SongService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val playlistService: PlaylistService,
    private val songService: SongService,
    private val queueService: QueueService,
    private val playerService: PlayerService,
    private val queueState: DataStore<QueueState>
) : ViewModel() {

    val playerHelper by lazy {
        PlayerHelper(exoPlayer)
    }

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    val allSongs = songService.songs.map {
        it.filter { song -> song.location.contains(".mp3") }
    }.catch { exception ->
        Log.e("allSongs", "$exception")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val allAlbums = songService.albums
        .catch { exception ->
            Log.e("allAlbums", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val allArtists = songService.artists
        .catch { exception ->
            Log.e("allArtists", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val personsWithSongCount = songService.albumArtists
        .catch { exception ->
            Log.e("personsWithSongCount", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val playlistsWithSongCount = playlistService.playlists
        .catch { exception ->
            Log.e("playlistsWithSongCount", "$exception")
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val currentSong = queueService.currentSong

    val queue = mutableListOf<Song>()

    val repeatMode = queueService.repeatMode

    fun toggleRepeatMode() {
        queueService.updateRepeatMode(repeatMode.value.next())
    }

    private val _currentSongPlaying = MutableStateFlow<Boolean?>(null)
    val currentSongPlaying = _currentSongPlaying.asStateFlow()

    private val queueServiceListener = object : QueueService.Listener {
        override fun onAppend(song: Song) {
            viewModelScope.launch(Dispatchers.Default) { queue.add(song) }
        }

        override fun onAppend(songs: List<Song>) {
            viewModelScope.launch(Dispatchers.Default) { queue.addAll(songs) }
        }

        override fun onUpdate(updatedSong: Song, position: Int) {
            if (position < 0 || position >= queue.size) return
            viewModelScope.launch(Dispatchers.Default) { queue[position] = updatedSong }
        }

        override fun onMove(from: Int, to: Int) {
            if (from < 0 || to < 0 || from >= queue.size || to >= queue.size) return
            viewModelScope.launch(Dispatchers.Default) { queue.apply { add(to, removeAt(from)) } }
        }

        override fun onRemove(from: Int) {
            viewModelScope.launch(Dispatchers.Default) { queue.apply { removeAt(from) } }
        }

        override fun onClear() {
            viewModelScope.launch(Dispatchers.Default) { queue.clear() }
        }

        override fun onSetQueue(songs: List<Song>, startPlayingFromPosition: Int) {
            viewModelScope.launch(Dispatchers.Default) {
                queue.apply {
                    clear()
                    addAll(songs)
                }
            }
        }
    }

    private val exoPlayerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            _currentSongPlaying.update { isPlaying }
        }
    }

    init {
        _currentSongPlaying.update { exoPlayer.isPlaying }
        exoPlayer.addListener(exoPlayerListener)
        queue.addAll(queueService.queue)
        queueService.addListener(queueServiceListener)
        viewModelScope.launch {
            currentSong.collectLatest {
                updatePlayCount(it)
            }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _message.update { message }
            delay(Constants.MESSAGE_DURATION)
            _message.update { "" }
        }
    }

    fun currentAudioProgress() = flow {
        while (true) {
            emit(
                withContext(Dispatchers.Main) {
                    playerHelper.currentPosition.toLong()
                }
            )
            delay(33)
        }
    }.flowOn(Dispatchers.IO)


    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(exoPlayerListener)
        queueService.removeListener(queueServiceListener)
    }

    /**
     * Shuffle the queue and start playing from first song
     */
    fun shufflePlay(songs: List<Song>?) = setQueue(songs?.shuffled(), 0)

    fun onPlaylistCreate(playlistName: String) {
        viewModelScope.launch {
            playlistService.createPlaylist(playlistName)
        }
    }

    fun updatePlaylistName(playlistWithSongCount: PlaylistWithSongCount) {
        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    playlistId = playlistWithSongCount.playlistId,
                    playlistName = playlistWithSongCount.playlistName,
                    createdAt = playlistWithSongCount.createdAt
                )
                playlistService.updatePlaylist(playlist)
                showMessage("Done")
            } catch (e: Exception) {
                Log.e("updatePlaylistName", "$e")
                showMessage("Some error occurred")
            }
        }
    }

    fun deletePlaylist(playlistWithSongCount: PlaylistWithSongCount) {
        viewModelScope.launch {
            try {
                playlistService.deletePlaylist(playlistWithSongCount.playlistId)
                showMessage("Done")
            } catch (e: Exception) {
                showMessage("Some error occurred")
            }
        }
    }

    fun addSongToPlaylist(playListId: Long, location: String) {
        viewModelScope.launch {
            try {
                val l = playlistService.addSongsToPlaylist(listOf(location), playListId)
                if (l.isNotEmpty()) {
                    if (l[0] > 0) {
                        showMessage("Song Added")
                    } else {
                        showMessage("Already Added")
                    }
                } else {
                    showMessage("Some error occurred")
                }
            } catch (e: Exception) {
                showMessage("Some error occurred")
            }
        }
    }

    fun addSongsToPlaylist(playListId: Long, songLocations: List<String>) {
        viewModelScope.launch {
            try {
                val l = playlistService.addSongsToPlaylist(songLocations, playListId)
                if (l.isNotEmpty()) {
                    if (l[0] > 0) {
                        showMessage("Songs Added")
                    } else {
                        showMessage("Already Added")
                    }
                } else {
                    showMessage("Some error occurred")
                }
            } catch (e: Exception) {
                showMessage("Some error occurred")
            }
        }
    }

    /**
     * Adds a song to the end of queue
     */
    fun addToQueue(song: Song) {
        if (queue.isEmpty()) {
            viewModelScope.launch {
                playerService.startServiceIfNotRunning(listOf(song), 0)
            }
        } else {
            val result = queueService.append(song)
            if (result) {
                showMessage("Added ${song.title} to queue")
            } else {
                showMessage("Song already in queue")
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
        viewModelScope.launch {
            playerService.startServiceIfNotRunning(songs, startPlayingFromIndex)
        }
        showMessage("Playing")
    }

    fun playPlaylistSongs(playlistWithSongCount: PlaylistWithSongCount) {
        viewModelScope.launch {
            playlistService.getPlaylistWithSongsById(
                playlistWithSongCount.playlistId
            ).first()?.let {
                setQueue(it.songs, 0)
            }
        }
    }

    private fun updatePlayCount(song: Song? = currentSong.value) {
        if (song == null) return
        val updatedSong = song.copy(playCount = song.playCount + 1)
        viewModelScope.launch(Dispatchers.IO) {
            songService.updateSong(updatedSong)
        }
    }

    /**
     * Toggle the favourite value of a song
     */
    fun changeFavouriteValue(song: Song? = currentSong.value) {
        if (song == null) return
        val updatedSong = song.copy(favourite = !song.favourite)
        viewModelScope.launch(Dispatchers.IO) {
            queueService.update(updatedSong)
            songService.updateSong(updatedSong)
        }
    }

    fun onSongDrag(fromIndex: Int, toIndex: Int) = queueService.moveSong(fromIndex, toIndex)

    fun onSongRemoveFromQueue(fromIndex: Int) = queueService.removeSong(fromIndex)

    fun setLastPlayedList() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = queueState.data.first()
            val songs = songService.getSongsFromLocations(state.locationsList)
            val locationMap = buildMap {
                for (song in songs) {
                    put(song.location, song)
                }
            }
            val orderedSongs = buildList {
                for (location in state.locationsList) {
                    if (locationMap.containsKey(location)) {
                        add(locationMap[location]!!)
                    }
                }
            }
            if (orderedSongs.isNotEmpty()) {
                val pos = state.startIndex
                queueService.setQueue(orderedSongs, pos)
            }
        }
    }

    fun isServiceInitialized(): Boolean {
        return playerService.isServiceRunning()
    }

    fun startPlaying() {
        viewModelScope.launch {
            val state = queueState.data.first()
            val pos = state.startIndex
            playerService.startServiceIfNotRunning(queueService.queue, pos)
        }
    }
}