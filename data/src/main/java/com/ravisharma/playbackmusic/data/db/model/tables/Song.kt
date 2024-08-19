package com.ravisharma.playbackmusic.data.db.model.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ravisharma.playbackmusic.data.utils.Constants

@Entity(
    tableName = Constants.Tables.SONG_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["name"],
            childColumns = ["album"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["name"],
            childColumns = ["artist"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["genre"],
            childColumns = ["genre"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
        ForeignKey(
            entity = AlbumArtist::class,
            parentColumns = ["name"],
            childColumns = ["albumArtist"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
        ForeignKey(
            entity = Lyricist::class,
            parentColumns = ["name"],
            childColumns = ["lyricist"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
        ForeignKey(
            entity = Composer::class,
            parentColumns = ["name"],
            childColumns = ["composer"],
            onDelete = ForeignKey.SET_DEFAULT,
        ),
    ]
)
data class Song(
    @PrimaryKey @ColumnInfo(name = "location") val location: String = "",
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "album") val album: String = "",
    @ColumnInfo(name = "size") val size: String,
    @ColumnInfo(name = "addedDate") val addedDate: String,
    @ColumnInfo(name = "modifiedDate") val modifiedDate: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "artist") val artist: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "albumArtist") val albumArtist: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "composer") val composer: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "genre") val genre: String,
    @ColumnInfo(index = true, defaultValue = "Unknown", name = "lyricist") val lyricist: String,
    @ColumnInfo(name = "year") val year: Int,
    @ColumnInfo(name = "comment") val comment: String? = null,
    @ColumnInfo(name = "durationMillis") val durationMillis: Long,
    @ColumnInfo(name = "durationFormatted") val durationFormatted: String,
    @ColumnInfo(name = "bitrate") val bitrate: Float,
    @ColumnInfo(name = "sampleRate") val sampleRate: Float,
    @ColumnInfo(name = "bitsPerSample") val bitsPerSample: Int = 0,
    @ColumnInfo(name = "mimeType") val mimeType: String? = null,
    @ColumnInfo(name = "favourite") val favourite: Boolean = false,
    @ColumnInfo(name = "artUri") val artUri: String? = null,
    @ColumnInfo(defaultValue = "0", name = "playCount") val playCount: Int = 0,
    @ColumnInfo(name = "lastPlayed") val lastPlayed: Long? = null,
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