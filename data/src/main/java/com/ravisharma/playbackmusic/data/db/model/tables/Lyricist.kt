package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.utils.Constants

@Entity(tableName = Constants.Tables.LYRICIST_TABLE)
data class Lyricist(
    @PrimaryKey val name: String,
)