package com.ravisharma.playbackmusic.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.model.Song

@Entity(tableName = "lastPlayed")
data class LastPlayed(
        @Embedded var song: Song
) {
    @ColumnInfo(name = "playedId")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}