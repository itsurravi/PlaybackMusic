package com.ravisharma.playbackmusic.database.model.new_data.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.utils.Constants

@Entity(tableName = Constants.Tables.COMPOSER_TABLE)
data class Composer(
    @PrimaryKey val name: String,
)
