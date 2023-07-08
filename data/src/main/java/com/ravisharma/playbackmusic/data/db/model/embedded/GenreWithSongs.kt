package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Genre
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class GenreWithSongs(
    @Embedded
    val genre: Genre,
    @Relation(
        parentColumn = "genre",
        entityColumn = "genre"
    )
    val songs: List<Song>
)
