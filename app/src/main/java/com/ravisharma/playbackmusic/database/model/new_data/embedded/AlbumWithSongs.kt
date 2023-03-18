package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Album
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class AlbumWithSongs(
    @Embedded
    val album: Album,
    @Relation(
        parentColumn = "name",
        entityColumn = "album"
    )
    val songs: List<Song2>,
)
