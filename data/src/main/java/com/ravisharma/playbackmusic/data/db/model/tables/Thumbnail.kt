package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.ColumnInfo
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
    @PrimaryKey @ColumnInfo(name = "location") val location: String,
    @ColumnInfo(name = "addedOn") val addedOn: Long,
    @ColumnInfo(name = "artCount") val artCount: Int,
    @ColumnInfo(name = "deleteThis") val deleteThis: Boolean,
)
