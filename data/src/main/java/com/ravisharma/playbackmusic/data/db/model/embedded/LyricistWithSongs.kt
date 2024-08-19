package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Lyricist
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class LyricistWithSongs(
    @Embedded
    val lyricist: Lyricist,
    @Relation(
        parentColumn = "name",
        entityColumn = "lyricist"
    )
    val songs: List<Song>,
)
