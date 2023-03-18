package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Composer
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class ComposerWithSongs(
    @Embedded
    val composer: Composer,
    @Relation(
        parentColumn = "name",
        entityColumn = "composer"
    )
    val songs: List<Song2>,
)
