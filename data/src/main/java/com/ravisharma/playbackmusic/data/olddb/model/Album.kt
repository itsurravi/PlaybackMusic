package com.ravisharma.playbackmusic.data.olddb.model

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class Album(
        @field:SerializedName("albumId") val albumId: Long,
        @field:SerializedName("albumArt") val albumArt: Uri,
        @field:SerializedName("albumName") val albumName: String,
        @field:SerializedName("albumArtist") val albumArtist: String,
        @field:SerializedName("numberOfSongs") val numberOfSongs: String
)