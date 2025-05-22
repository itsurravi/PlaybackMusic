package com.ravisharma.playbackmusic.data.olddb.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.olddb.model.Song

@Entity(tableName = "mostPlayed")
data class MostPlayed(
        @Embedded var song: Song,
        @ColumnInfo(name = "playedCount")
        var playCount: Long
) {
    @ColumnInfo(name = "playedId")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}