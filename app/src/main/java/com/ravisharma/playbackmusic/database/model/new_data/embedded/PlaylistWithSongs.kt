package com.ravisharma.playbackmusic.database.model.new_data.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ravisharma.playbackmusic.database.model.new_data.tables.Playlist2
import com.ravisharma.playbackmusic.database.model.new_data.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.database.model.new_data.tables.Song2

data class PlaylistWithSongs(
    @Embedded
    val playlist: Playlist2,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "location",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<Song2>
)
