package com.ravisharma.playbackmusic.data.olddb.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.data.olddb.database.model.LastPlayed

@Dao
interface LastPlayedDao {

    @Insert
    fun addToLastPlayedList(lastPlayed: LastPlayed)

    @Query("DELETE FROM lastPlayed WHERE id=:songId")
    fun deleteLastPlayedSong(songId: Long)

    @Query("SELECT COUNT(*) FROM lastPlayed WHERE id =:songId")
    fun checkSongIfExist(songId: Long): Long

    @Query("SELECT * FROM lastPlayed ORDER BY playedId DESC LIMIT 30")
    fun getAllLastPlayedSongs(): LiveData<List<LastPlayed>>
}