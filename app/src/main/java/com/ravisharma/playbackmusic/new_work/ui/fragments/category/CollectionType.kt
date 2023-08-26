package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CollectionType(val type: Int, val id: String = "") : Parcelable {
    companion object {
        const val Category = "Category"
        const val AlbumType = 0
        const val ArtistType = 1
        const val PlaylistType = 2
        const val FavouritesType = 3
    }
}