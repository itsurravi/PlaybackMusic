package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.new_work.services.DataManager
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val context: Application,
    private val manager: DataManager,
    private val dataProvider: DataProvider,
) : ViewModel() {

    val currentSong = manager.currentSong
    val queue = manager.queue

    private val _collectionType = MutableStateFlow<CollectionType?>(null)
    val collectionType = _collectionType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionUi = _collectionType.flatMapLatest { type ->
        when (type?.type) {
            CollectionType.RecentAddedType -> {
                dataProvider.findCollection.getRecentAdded().map {
                    CollectionUi(
                        songs = it,
                        topBarTitle = "Recent Added",
                        topBarBackgroundImageUri = it[0].artUri ?: ""
                    )
                }
            }

            CollectionType.AlbumType -> {
                dataProvider.findCollection.getAlbumWithSongsByName(type.id).map {
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
                dataProvider.findCollection.getArtistWithSongsByName(type.id).map {
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

            CollectionType.FavouritesType -> {
                dataProvider.findCollection.getFavourites().map {
                    CollectionUi(
                        songs = it,
                        topBarTitle = "Favourites",
                        topBarBackgroundImageUri = it.randomOrNull()?.artUri ?: ""
                    )
                }
            }

            CollectionType.PlaylistType -> {
                dataProvider.findCollection.getPlaylistWithSongsById(type.id.toLong()).map {
                    if (it == null) CollectionUi()
                    else {
                        CollectionUi(
                            songs = it.songs,
                            topBarTitle = it.playlist.playlistName,
                            topBarBackgroundImageUri = it.songs.randomOrNull()?.artUri ?: ""
                        )
                    }
                }
            }

            else -> flow { }
        }
    }

    fun loadCollection(type: CollectionType?) {
        _collectionType.update { type }
    }

    fun setQueue(songs: List<Song>?, startPlayingFromIndex: Int = 0) {
        if (songs == null) return
        manager.setQueue(songs, startPlayingFromIndex)
        context.showToast("Playing")
    }

    fun addToQueue(song: Song) {
        if (queue.isEmpty()) {
            manager.setQueue(listOf(song), 0)
        } else {
            val result = manager.addToQueue(song)
            if (result) {
                context.showToast("Added ${song.title} to queue")
            } else {
                context.showToast("Song already in queue")
            }
        }
    }

    fun addToQueue(songs: List<Song>) {
        if (queue.isEmpty()) {
            manager.setQueue(songs, 0)
        } else {
            var result = false
            songs.forEach { result = result or manager.addToQueue(it) }
            if (result) {
                context.showToast("Done")
            } else {
                context.showToast("Songs already in queue")
            }
        }
    }

    fun addNextInQueue(song: Song) {
        if (queue.isEmpty()) {
            context.showToast("No song in queue")
        } else {
            val result = manager.addNextInQueue(song)
            if (result) {
                context.showToast("Added ${song.title} to queue")
            }
        }
    }

    fun changeFavouriteValue(song: Song? = currentSong.value) {
        if (song == null) return
        val updatedSong = song.copy(favourite = !song.favourite)
        viewModelScope.launch(Dispatchers.IO) {
            manager.updateSong(updatedSong)
        }
    }

    fun removeFromPlaylist(song: Song) {
        viewModelScope.launch {
            try {
                val playlistId =
                    _collectionType.value?.id?.toLong() ?: throw IllegalArgumentException()
                val playlistSongCrossRef = PlaylistSongCrossRef(playlistId, song.location)
                dataProvider.deletePlaylistSongCrossRef(playlistSongCrossRef)
                context.showToast("Removed")
            } catch (e: Exception) {
                context.showToast("Some error occurred")
            }
        }
    }
}