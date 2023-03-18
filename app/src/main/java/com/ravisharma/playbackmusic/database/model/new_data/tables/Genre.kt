package com.ravisharma.playbackmusic.database.model.new_data.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.utils.Constants

@Entity(tableName = Constants.Tables.GENRE_TABLE)
data class Genre(
    @PrimaryKey val genre: String,
)
