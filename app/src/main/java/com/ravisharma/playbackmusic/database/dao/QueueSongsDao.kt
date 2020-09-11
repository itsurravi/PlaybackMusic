package com.ravisharma.playbackmusic.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.database.model.QueueSongs

@Dao
interface QueueSongsDao {
    @Insert
    fun addSongs(songs: QueueSongs)

    @Query("DELETE FROM queueSongs WHERE id=:id")
    fun deleteFromQueue(id: Long)

    @Query("DELETE FROM queueSongs")
    fun removeAll() : Int

    @get:Query("SELECT * FROM queueSongs")
    val queueSongs: List<QueueSongs>
}