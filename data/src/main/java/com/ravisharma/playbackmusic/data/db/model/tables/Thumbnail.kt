package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.utils.Constants

@Entity(
    tableName = Constants.Tables.THUMBNAIL_TABLE,
    indices = [
        Index(value = ["location"], unique = true)
    ]
)
data class Thumbnail(
    @PrimaryKey val location: String,
    val addedOn: Long,
    val artCount: Int,
    val deleteThis: Boolean,
)
