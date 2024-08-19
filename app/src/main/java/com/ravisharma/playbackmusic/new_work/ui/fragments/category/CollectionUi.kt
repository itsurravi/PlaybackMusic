package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import com.ravisharma.playbackmusic.data.db.model.tables.Song

data class CollectionUi(
    val error: String? = null,
    val songs: List<Song> = listOf(),
    val topBarTitle: String = "",
    val topBarBackgroundImageUri: String = "",
)