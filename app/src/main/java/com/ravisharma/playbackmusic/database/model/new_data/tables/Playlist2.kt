package com.ravisharma.playbackmusic.database.model.new_data.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.utils.Constants

@Entity(tableName = Constants.Tables.PLAYLIST_TABLE)
data class Playlist2(
    @PrimaryKey(autoGenerate = true) val playlistId: Long,
    val playlistName: String,
    val createdAt: Long,
)

data class PlaylistExceptId(
    val playlistName: String,
    val createdAt: Long,
)
