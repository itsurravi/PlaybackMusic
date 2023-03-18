package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Lyricist
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class LyricistWithSongs(
    @Embedded
    val lyricist: Lyricist,
    @Relation(
        parentColumn = "name",
        entityColumn = "lyricist"
    )
    val songs: List<Song2>,
)
