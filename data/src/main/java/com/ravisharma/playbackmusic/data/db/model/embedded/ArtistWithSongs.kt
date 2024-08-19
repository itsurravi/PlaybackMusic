package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Artist
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class ArtistWithSongs(
    @Embedded
    val artist: Artist,
    @Relation(
        parentColumn = "name",
        entityColumn = "artist"
    )
    val songs: List<Song>,
)
