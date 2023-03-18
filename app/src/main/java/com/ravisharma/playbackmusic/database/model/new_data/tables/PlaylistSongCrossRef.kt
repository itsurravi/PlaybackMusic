package com.ravisharma.playbackmusic.database.model.new_data.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.ravisharma.playbackmusic.utils.Constants

@Entity(
    primaryKeys = ["playlistId","location"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist2::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song2::class,
            parentColumns = ["location"],
            childColumns = ["location"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    tableName = Constants.Tables.PLAYLIST_SONG_CROSS_REF_TABLE,
)
data class PlaylistSongCrossRef(
    val playlistId: Long,

    // refers to location of song
    @ColumnInfo(index = true)
    val location: String,
)
