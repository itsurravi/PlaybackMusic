package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.ComposerWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Composer
import com.ravisharma.playbackmusic.data.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface ComposerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllComposers(data: List<Composer>)

    @Query("SELECT * FROM ${Constants.Tables.COMPOSER_TABLE} WHERE name LIKE '%' || :query || '%'")
    suspend fun searchComposers(query: String): List<Composer>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.COMPOSER_TABLE} WHERE name = :name")
    fun getComposerWithSongs(name: String): Flow<ComposerWithSongs?>

}