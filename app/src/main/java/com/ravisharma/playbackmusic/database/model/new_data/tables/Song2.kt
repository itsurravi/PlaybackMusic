package com.ravisharma.playbackmusic.database.model.new_data.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.utils.Constants

@Entity(
    tableName = Constants.Tables.SONG_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["name"],
            childColumns = ["album"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["name"],
            childColumns = ["artist"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Song2(
    @PrimaryKey val location: String = "",
    val title: String,
    @ColumnInfo(index = true) val album: String = "",
    val size: String,
    val addedDate: String,
    val modifiedDate: String,
    @ColumnInfo(index = true) val artist: String,
    @ColumnInfo(index = true) val albumArtist: String,
    @ColumnInfo(index = true) val composer: String,
    @ColumnInfo(index = true) val genre: String,
    @ColumnInfo(index = true) val lyricist: String,
    val year: Int,
    val durationMillis: Long,
    val durationFormatted: String,
    val mimeType: String? = null,
    val artUri: String? = null,
    val favourite: Boolean = false
){
    data class Metadata(
        val artist: String,
        val albumArtist: String,
        val composer: String,
        val genre: String,
        val lyricist: String,
        val year: Int,
        val comment: String? = null,
        val duration: Long,
        val bitrate: Float,
        val sampleRate: Float,
        val bitsPerSample: Int = 0,
        val mimeType: String? = null,
    )
}
