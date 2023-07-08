package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class AlbumWithSongs(
    @Embedded
    val album: Album,
    @Relation(
        parentColumn = "name",
        entityColumn = "album"
    )
    val songs: List<Song>,
)
