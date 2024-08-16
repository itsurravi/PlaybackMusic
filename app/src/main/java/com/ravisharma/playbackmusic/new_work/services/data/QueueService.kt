package com.ravisharma.playbackmusic.new_work.services.data

import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.new_work.ui.fragments.now.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.TreeSet

interface QueueService {
    val queue: List<Song>

    val currentSong: StateFlow<Song?>

    fun append(song: Song): Boolean
    fun append(songs: List<Song>): Boolean
    fun update(song: Song): Boolean
    fun moveSong(initialPosition: Int, finalPosition: Int): Boolean
    fun removeSong(from: Int)

    fun clearQueue()
    fun setQueue(songs: List<Song>, startPlayingFromPosition: Int)

    fun getSongAtIndex(index: Int): Song?

    fun setCurrentSong(currentSongIndex: Int)

    val repeatMode: StateFlow<RepeatMode>

    fun updateRepeatMode(mode: RepeatMode)

    interface Listener {
        fun onAppend(song: Song)
        fun onAppend(songs: List<Song>)
        fun onUpdate(updatedSong: Song, position: Int)
        fun onMove(from: Int, to: Int)
        fun onRemove(from: Int)
        fun onClear()
        fun onSetQueue(songs: List<Song>, startPlayingFromPosition: Int)
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}

class QueueServiceImpl() : QueueService {

    internal val mutableQueue = ArrayList<Song>()

    internal val locations = TreeSet<String>()

    override val queue: List<Song>
        get() = mutableQueue.toList()

    internal val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    override fun append(song: Song): Boolean {
        if (locations.contains(song.location)) return false
        locations.add(song.location)
        mutableQueue.add(song)
        callbacks.forEach { it.onAppend(song) }
        return true
    }

    override fun append(songs: List<Song>): Boolean {
        val songsNotInQueue = songs.filter { !locations.contains(it.location) }
        if (songsNotInQueue.isEmpty()) return false
        songsNotInQueue.forEach {
            mutableQueue.add(it)
            locations.add(it.location)
        }
        callbacks.forEach { it.onAppend(songs) }
        return true
    }

    override fun update(song: Song): Boolean {
        if (!locations.contains(song.location)) return false
        var position = -1
        for (idx in mutableQueue.indices) {
            if (mutableQueue[idx].location == song.location) {
                mutableQueue[idx] = song
                position = idx
                break
            }
        }
        if (song.location == _currentSong.value?.location) {
            _currentSong.update { song }
        }
        if (position != -1) {
            callbacks.forEach { it.onUpdate(song, position) }
        }
        return true
    }

    override fun moveSong(initialPosition: Int, finalPosition: Int): Boolean {
        if (initialPosition < 0 || initialPosition >= mutableQueue.size) return false
        if (finalPosition < 0 || finalPosition >= mutableQueue.size) return false
        if (initialPosition == finalPosition) return false
        mutableQueue.apply {
            add(finalPosition, removeAt(initialPosition))
        }
        callbacks.forEach { it.onMove(initialPosition, finalPosition) }
        return true
    }

    override fun removeSong(from: Int) {
        mutableQueue.apply {
            removeAt(from)
        }
        callbacks.forEach { it.onRemove(from) }
    }

    override fun clearQueue() {
        mutableQueue.clear()
        _currentSong.update { null }
        locations.clear()
        callbacks.forEach { it.onClear() }
    }

    override fun setQueue(songs: List<Song>, startPlayingFromPosition: Int) {
        if (songs.isEmpty()) return
        mutableQueue.apply {
            clear()
            addAll(songs)
        }
        val idx = if (startPlayingFromPosition >= songs.size || startPlayingFromPosition < 0) 0
        else startPlayingFromPosition
        _currentSong.update { mutableQueue[idx] }
        locations.clear()
        songs.forEach { locations.add(it.location) }
        callbacks.forEach { it.onSetQueue(songs, idx) }
    }

    override fun getSongAtIndex(index: Int): Song? {
        if (index < 0 || index >= mutableQueue.size) return null
        return mutableQueue[index]
    }

    override fun setCurrentSong(currentSongIndex: Int) {
        if (currentSongIndex < 0 || currentSongIndex >= mutableQueue.size) return
        _currentSong.update { mutableQueue[currentSongIndex] }
    }

    private val _repeatMode = MutableStateFlow(RepeatMode.NO_REPEAT)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    override fun updateRepeatMode(mode: RepeatMode) {
        _repeatMode.update { mode }
    }

    private val callbacks = ArrayList<QueueService.Listener>()

    override fun addListener(listener: QueueService.Listener) {
        callbacks.add(listener)
    }

    override fun removeListener(listener: QueueService.Listener) {
        callbacks.remove(listener)
    }
}