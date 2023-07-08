package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.LyricistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Lyricist
import com.ravisharma.playbackmusic.data.utils.Constants
import kotlinx.coroutines.flow.Flow


@Dao
interface LyricistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllLyricists(data: List<Lyricist>)

    @Query("SELECT * FROM ${Constants.Tables.LYRICIST_TABLE} WHERE name LIKE '%' || :query || '%'")
    suspend fun searchLyricists(query: String): List<Lyricist>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.LYRICIST_TABLE} WHERE name = :name")
    fun getLyricistWithSongs(name: String): Flow<LyricistWithSongs?>

}