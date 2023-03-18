package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.Composer
import com.ravisharma.playbackmusic.database.model.new_data.embedded.ComposerWithSongs
import com.ravisharma.playbackmusic.utils.Constants
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