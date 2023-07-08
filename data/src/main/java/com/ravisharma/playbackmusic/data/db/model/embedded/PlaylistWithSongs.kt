package com.ravisharma.playbackmusic.data.db.model.embedded

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class PlaylistWithSongs(
    @Embedded
    val playlist: Playlist,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "location",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<Song>
)
