package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.utils.Constants

@Entity(tableName = Constants.Tables.GENRE_TABLE)
data class Genre(
    @PrimaryKey @ColumnInfo(name = "genre") val genre: String,
)