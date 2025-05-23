package com.ravisharma.playbackmusic.data.olddb.database.repository

import androidx.lifecycle.LiveData
import com.ravisharma.playbackmusic.data.olddb.database.dao.LastPlayedDao
import com.ravisharma.playbackmusic.data.olddb.database.model.LastPlayed
import com.ravisharma.playbackmusic.data.olddb.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LastPlayedRepository @Inject constructor(
    private val lastPlayedDao: LastPlayedDao
) {

//    private val lastPlayedDao: LastPlayedDao
//    private val database: PlaylistDatabase

//    init {
//        database = PlaylistDatabase.getInstance(context)
//        lastPlayedDao = database.lastPlayedDao()
//    }

    fun addSongToLastPlayed(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            val existId = checkSongIfExist(song.id)
            if (existId > 0) {
                deleteSongFromLastPlayed(song.id)
            }
            val lastPlayed = LastPlayed(song)
            addSongToLastPlayed(lastPlayed)
        }
    }

    private fun addSongToLastPlayed(lastPlayed: LastPlayed) {
        lastPlayedDao.addToLastPlayedList(lastPlayed)
    }

    fun deleteSongFromLastPlayed(songId: Long) {
        lastPlayedDao.deleteLastPlayedSong(songId)
    }

    private fun checkSongIfExist(songId: Long): Long {
        return lastPlayedDao.checkSongIfExist(songId)
    }

    fun getLastPlayedSongsList(): LiveData<List<LastPlayed>> {
        return lastPlayedDao.getAllLastPlayedSongs()
    }
}