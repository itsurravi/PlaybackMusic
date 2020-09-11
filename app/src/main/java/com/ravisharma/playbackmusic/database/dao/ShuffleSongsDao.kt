package com.ravisharma.playbackmusic.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.database.model.ShuffleSongs

@Dao
interface ShuffleSongsDao {
    @Insert
    fun addSong(songs: ShuffleSongs)

    @Query("DELETE FROM shuffledSongs WHERE id=:id")
    fun deleteFromShuffle(id: Long)

    @Query("DELETE FROM shuffledSongs")
    fun removeAll() : Int

    @get:Query("SELECT * FROM shuffledSongs")
    val shuffleSongs: List<ShuffleSongs>
}