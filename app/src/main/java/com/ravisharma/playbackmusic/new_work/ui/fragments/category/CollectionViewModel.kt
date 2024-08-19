package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.data.utils.Constants
import com.ravisharma.playbackmusic.new_work.services.DataManager
import com.ravisharma.playbackmusic.new_work.services.data.PlayerService
import com.ravisharma.playbackmusic.new_work.services.data.PlaylistService
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.SongService
import com.ravisharma.playbackmusic.new_work.ui.extensions.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val playlistService: PlaylistService,
    private val songService: SongService,
    private val playerService: PlayerService,
    private val queueService: QueueService,
) : ViewModel() {

    val currentSong = queueService.currentSong

    private val isQueueEmpty: Boolean
        get() = currentSong.value == null

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _collectionType = MutableStateFlow<CollectionType?>(null)
    val collectionType = _collectionType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionUi = _collectionType
        .flatMapLatest { type ->
            when (type?.type) {
                CollectionType.MostPlayedType -> {
                    songService.getMostPlayedSongs().map {
                        if (it.isEmpty()) CollectionUi(
                            topBarTitle = "Most Played",
                        )
                        else {
                            CollectionUi(
                                songs = it,
                                topBarTitle = "Most Played",
                                topBarBackgroundImageUri = it[0].artUri ?: ""
                            )
                        }
                    }
                }
                CollectionType.RecentAddedType -> {
                    songService.getRecentAddedSongs().map {
                        CollectionUi(
                            songs = it,
                            topBarTitle = "Recent Added",
                            topBarBackgroundImageUri = it[0].artUri ?: ""
                        )
                    }
                }
                CollectionType.AlbumType -> {
                    songService.getAlbumWithSongsByName(type.id).map {
                        if (it == null) CollectionUi()
                        else {
                            CollectionUi(
                                songs = it.songs,
                                topBarTitle = it.album.name,
                                topBarBackgroundImageUri = it.album.albumArtUri ?: ""
                            )
                        }
                    }
                }
                CollectionType.ArtistType -> {
                    songService.getArtistWithSongsByName(type.id).map {
                        if (it == null) CollectionUi()
                        else {
                            CollectionUi(
                                songs = it.songs,
                                topBarTitle = it.artist.name,
                                topBarBackgroundImageUri = it.songs.randomOrNull()?.artUri ?: ""
                            )
                        }
                    }
                }
                CollectionType.PlaylistType -> {
                    playlistService.getPlaylistWithSongsById(type.id.toLong()).map {
                        if (it == null) CollectionUi()
                        else {
                            CollectionUi(
                                songs = it.songs,
                                topBarTitle = it.playlist.playlistName,
                                topBarBackgroundImageUri = it.playlist.artUri ?:
                                it.songs.randomOrNull()?.artUri ?: ""
                            )
                        }
                    }
                }
                CollectionType.FavouritesType -> {
                    songService.getFavouriteSongs().map {
                        CollectionUi(
                            songs = it,
                            topBarTitle = "Favourites",
                            topBarBackgroundImageUri = it.randomOrNull()?.artUri ?: ""
                        )
                    }
                }
                else -> flow { }
            }
        }.catch { exception ->
            exception.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(100),
            initialValue = null
        )

    fun loadCollection(type: CollectionType?) {
        _collectionType.update { type }
    }

    fun setQueue(songs: List<Song>?, startPlayingFromIndex: Int = 0) {
        if (songs == null) return
        viewModelScope.launch {
            playerService.startServiceIfNotRunning(songs, startPlayingFromIndex)
        }
        showMessage("Playing")
    }

    fun addToQueue(song: Song) {
        if (isQueueEmpty) {
            viewModelScope.launch {
                playerService.startServiceIfNotRunning(listOf(song), 0)
            }
        } else {
            val result = queueService.append(song)
            showMessage(
                if (result) "Added ${song.title} to queue"
                else "Song already in queue"
            )
        }
    }

    fun addToQueue(songs: List<Song>) {
        if (isQueueEmpty) {
            viewModelScope.launch {
                playerService.startServiceIfNotRunning(songs, 0)
                showMessage("Playing")
            }
        } else {
            val result = queueService.append(songs)
            showMessage(if (result) "Done" else "Songs already in queue")
        }
    }

    fun changeFavouriteValue(song: Song? = currentSong.value) {
        if (song == null) return
        val updatedSong = song.copy(favourite = !song.favourite)
        viewModelScope.launch(Dispatchers.IO) {
            queueService.update(updatedSong)
            songService.updateSong(updatedSong)
        }
    }

    fun removeFromPlaylist(song: Song){
        viewModelScope.launch {
            try {
                val playlistId = _collectionType.value?.id?.toLong() ?: throw IllegalArgumentException()
                playlistService.removeSongsFromPlaylist(listOf(song.location), playlistId)
                showMessage("Removed")
            } catch (e: Exception){
                showMessage("Some error occurred")
            }
        }
    }

    private fun showMessage(message: String){
        viewModelScope.launch {
            _message.update { message }
            delay(Constants.MESSAGE_DURATION)
            _message.update { "" }
        }
    }
}