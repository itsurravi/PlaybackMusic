package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.utils.Constants

@Entity(tableName = Constants.Tables.ALBUM_ARTIST_TABLE)
data class AlbumArtist(
    @PrimaryKey @ColumnInfo(name = "name") val name: String
)
