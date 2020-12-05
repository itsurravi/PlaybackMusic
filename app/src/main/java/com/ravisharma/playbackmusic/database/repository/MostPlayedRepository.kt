package com.ravisharma.playbackmusic.database.repository

import android.content.Context
import com.ravisharma.playbackmusic.database.PlaylistDatabase
import com.ravisharma.playbackmusic.database.dao.MostPlayedDao
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MostPlayedRepository(context: Context) {

    private val mostPlayedDao: MostPlayedDao
    private val database: PlaylistDatabase

    init {
        database = PlaylistDatabase.getInstance(context)
        mostPlayedDao = database.mostPlayedDao()
    }

    fun addSongToMostPlayed(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            val existId = checkSongIfExist(song.id)
            if (existId > 0) {
                val playedId = getPlayedId(song.id)
                updatePlayCount(playedId)
            } else {
                val mostPlayed = MostPlayed(song, 1)
                addSongToMostPlayed(mostPlayed)
            }
        }
    }

    private fun addSongToMostPlayed(mostPlayed: MostPlayed) {
        mostPlayedDao.addToMostPlayedList(mostPlayed)
    }

    private fun checkSongIfExist(songId: Long): Long {
        return mostPlayedDao.checkSongIfExist(songId)
    }

    private fun getPlayedId(songId: Long): Long {
        return mostPlayedDao.getPlayedId(songId)
    }

    private fun updatePlayCount(playedId: Long) {
        mostPlayedDao.updatePlayCount(playedId)
    }

    fun getMostPlayedSongs(): List<MostPlayed> {
        return mostPlayedDao.getMostPlayedSongs()
    }
}