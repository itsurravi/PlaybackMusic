package com.ravisharma.playbackmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ravisharma.playbackmusic.data.db.model.embedded.GenreWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Genre
import com.ravisharma.playbackmusic.data.utils.Constants
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