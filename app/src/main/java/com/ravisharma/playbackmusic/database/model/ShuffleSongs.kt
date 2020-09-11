package com.ravisharma.playbackmusic.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.model.Song

@Entity(tableName = "shuffledSongs")
class ShuffleSongs(
        @field:ColumnInfo(name = "shuffleId")
        @field:PrimaryKey(autoGenerate = true) var id: Long,
        @field:Embedded var song: Song
)