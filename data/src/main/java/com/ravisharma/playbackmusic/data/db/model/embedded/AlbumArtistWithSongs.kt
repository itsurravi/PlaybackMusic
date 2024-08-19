package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.AlbumArtist
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class AlbumArtistWithSongs(
    @Embedded
    val albumArtist: AlbumArtist,
    @Relation(
        parentColumn = "name",
        entityColumn = "albumArtist"
    )
    val songs: List<Song>,
)
