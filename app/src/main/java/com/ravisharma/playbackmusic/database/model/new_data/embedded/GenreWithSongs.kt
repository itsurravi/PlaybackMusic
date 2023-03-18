package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Genre
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class GenreWithSongs(
    @Embedded
    val genre: Genre,
    @Relation(
        parentColumn = "genre",
        entityColumn = "genre"
    )
    val songs: List<Song2>
)
