package com.ravisharma.playbackmusic.data.olddb.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlistTable")
data class Playlist(
        @field:ColumnInfo(name = "playlistId")
        @field:PrimaryKey(autoGenerate = true) var id: Long,
        @field:ColumnInfo(name = "playlistName") var name: String,
        @field:Embedded var song: Song
)