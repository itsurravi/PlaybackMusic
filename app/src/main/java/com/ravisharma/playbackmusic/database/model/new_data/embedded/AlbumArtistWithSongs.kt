package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.AlbumArtist
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class AlbumArtistWithSongs(
    @Embedded
    val albumArtist: AlbumArtist,
    @Relation(
        parentColumn = "name",
        entityColumn = "albumArtist"
    )
    val songs: List<Song2>,
)
