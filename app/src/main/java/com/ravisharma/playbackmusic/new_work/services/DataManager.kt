package com.ravisharma.playbackmusic.new_work.services

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.data.utils.TinyDb
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.ui.fragments.now.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Named

class DataManager @Inject constructor(
    private val context: Context,
    private val dataProvider: DataProvider,
    private val tinyDb: TinyDb,
    @Named(value = "lastPlayedInfo") private val pref: SharedPreferences
) {

    private var callback: Callback? = null

    private val _queue = mutableListOf<Song>()
    val queue: List<Song> = _queue

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.NO_REPEAT)
    val repeatMode = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow<Boolean>(false)
    val shuffleMode = _shuffleMode.asStateFlow()

    fun updateRepeatMode(newRepeatMode: RepeatMode) {
        _repeatMode.update { newRepeatMode }
    }

    fun updateShuffleMode(newShuffleMode: Boolean) {
        _shuffleMode.update { newShuffleMode }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        _queue.apply { add(toIndex, removeAt(fromIndex)) }
    }

    suspend fun updateSong(song: Song) {
        if (_currentSong.value?.location == song.location) {
            _currentSong.update { song }
            callback?.updateNotification()
        }
        for (idx in _queue.indices) {
            if (_queue[idx].location == song.location) {
                _queue[idx] = song
                break
            }
        }
        dataProvider.updateSong(song)
    }

    private var remIdx = 0

    fun cleanData() {
        dataProvider.cleanData()
    }

    fun startPlayingLastList() {
        val index = pref.getInt(Constants.SongIndex, -1)
        if (index != -1 && index < _queue.size) {
            setQueue(_queue.map {
                it.copy()
            }, index)
        }
    }

    fun isServiceInitialized(): Boolean {
        return callback != null
    }

    @Synchronized
    fun setLastPlayedList() {
        if (_queue.isEmpty()) {
            _queue.apply {
                val list = tinyDb.getListObject(Constants.PlayingList, Song::class.java)
                clear()
                addAll(list)
            }
            val index = pref.getInt(Constants.SongIndex, -1)
            if (index != -1 && index < _queue.size) {
                _currentSong.value = _queue[index]
            }
        }
    }

    @Synchronized
    fun setQueue(newQueue: List<Song>, startPlayingFromIndex: Int) {
        if (newQueue.isEmpty()) return
        _queue.apply {
            clear()
            addAll(newQueue)
        }
        _currentSong.value = newQueue[startPlayingFromIndex]
        if (callback == null) {
            val intent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            remIdx = startPlayingFromIndex
        } else {
            callback?.setQueue(newQueue, startPlayingFromIndex)
        }
    }

    /**
     * Returns true if added to queue else returns false if already in queue
     */
    @Synchronized
    fun addToQueue(song: Song): Boolean {
        if (_queue.any { it.location == song.location }) return false
        _queue.add(song)
        callback?.addToQueue(song)
        return true
    }

    @Synchronized
    fun addNextInQueue(song: Song): Boolean {
        val index = callback?.addNextInQueue(song)
        return if (index != null) {
            _queue.add(index, song)
            true
        } else {
            false
        }
    }

    fun playOnPosition(position: Int) {
        callback?.playOnPosition(position)
    }

    fun setPlayerRunning(callback: Callback) {
        this.callback = callback
        this.callback?.setQueue(_queue, remIdx)
    }

    fun updateCurrentSong(currentSongIndex: Int) {
        if (currentSongIndex < 0 || currentSongIndex >= _queue.size) return
        _currentSong.update { _queue[currentSongIndex] }
    }

    fun getSongAtIndex(index: Int): Song? {
        if (index < 0 || index >= _queue.size) return null
        return _queue[index]
    }

    fun stopPlayerRunning(index: Int) {
        pref.edit().putInt(Constants.SongIndex, index).commit()
        tinyDb.putListObject(Constants.PlayingList, _queue.toList())

        this.callback = null
//        _currentSong.update { null }
//        _queue.clear()
    }

    interface Callback {
        fun setQueue(newQueue: List<Song>, startPlayingFromIndex: Int)
        fun addToQueue(song: Song)
        fun addNextInQueue(song: Song): Int
        fun updateNotification()
        fun updateExoList(shuffled: Boolean, list: List<Song>)
        fun playOnPosition(position: Int)
    }
}