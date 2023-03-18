package com.ravisharma.playbackmusic.database.dao.new_daos

import androidx.room.*
import com.ravisharma.playbackmusic.database.model.new_data.tables.Genre
import com.ravisharma.playbackmusic.database.model.new_data.embedded.GenreWithSongs
import com.ravisharma.playbackmusic.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllGenres(data: List<Genre>)

    @Query("SELECT * FROM ${Constants.Tables.GENRE_TABLE} WHERE genre LIKE '%' || :query || '%'")
    suspend fun searchGenres(query: String): List<Genre>

    @Transaction
    @Query("SELECT * FROM ${Constants.Tables.GENRE_TABLE} WHERE genre = :genreName")
    fun getGenreWithSongs(genreName: String): Flow<GenreWithSongs?>

}