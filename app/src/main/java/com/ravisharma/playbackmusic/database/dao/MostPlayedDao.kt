package com.ravisharma.playbackmusic.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.database.model.MostPlayed

@Dao
interface MostPlayedDao {

    @Insert
    fun addToMostPlayedList(mostPlayed: MostPlayed)

    @Query("SELECT COUNT(*) FROM mostPlayed WHERE id =:songId")
    fun checkSongIfExist(songId: Long): Long

    @Query("SELECT playedId FROM mostPlayed WHERE id=:songId")
    fun getPlayedId(songId: Long): Long

    @Query("UPDATE mostPlayed SET playedCount=playedCount+1 WHERE playedId=:playedId")
    fun updatePlayCount(playedId: Long)

    @Query("SELECT * FROM mostPlayed ORDER BY playedCount DESC, playedId DESC LIMIT 30")
    fun getMostPlayedSongs(): LiveData<List<MostPlayed>>

    @Query("DELETE FROM mostPlayed WHERE id=:songId")
    fun deleteMostPlayedSong(songId: Long)
}