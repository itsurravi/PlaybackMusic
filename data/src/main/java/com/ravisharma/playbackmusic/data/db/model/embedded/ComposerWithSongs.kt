package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Composer
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class ComposerWithSongs(
    @Embedded
    val composer: Composer,
    @Relation(
        parentColumn = "name",
        entityColumn = "composer"
    )
    val songs: List<Song>,
)
