package com.ravisharma.playbackmusic.data.olddb.model

import com.google.gson.annotations.SerializedName

data class Artist(
        @field:SerializedName("artistId") val artistId: Long,
        @field:SerializedName("artistName") val artistName: String,
        @field:SerializedName("numberOfAlbums") val numberOfAlbums: String,
        @field:SerializedName("numberOfTracks") val numberOfTracks: String
)