package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Artist
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class ArtistWithSongs(
    @Embedded
    val artist: Artist,
    @Relation(
        parentColumn = "name",
        entityColumn = "artist"
    )
    val songs: List<Song2>,
)
