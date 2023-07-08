package com.ravisharma.playbackmusic.data.provider

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.ravisharma.playbackmusic.data.db.dao.*
import com.ravisharma.playbackmusic.data.db.model.tables.*
import com.ravisharma.playbackmusic.data.db.model.MetadataExtractor
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.data.utils.formatToDate
import com.ravisharma.playbackmusic.data.utils.toMBfromB
import com.ravisharma.playbackmusic.data.utils.toMS
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

class DataScanner(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val albumArtistDao: AlbumArtistDao,
    private val composerDao: ComposerDao,
    private val lyricistDao: LyricistDao,
    private val genreDao: GenreDao,
) {
    private val _scanStatus = Channel<ScanStatus>()
    val scanStatus = _scanStatus.receiveAsFlow()

    suspend fun performMusicScan(contentResolver: ContentResolver) {
        _scanStatus.send(ScanStatus.ScanStarted)

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media._ID
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
            null
        ) ?: return
        val totalSongs = cursor.count
        var parsedSongs = 0
        cursor.moveToFirst()
        val mExtractor = MetadataExtractor()
        val dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val dateAddedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
        val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
        val songIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val songCover = Uri.parse("content://media/external/audio/albumart")

        val songs = ArrayList<Song>()
        val albumArtMap = HashMap<String, String?>()
        val artistSet = TreeSet<String>()
        val albumArtistSet = TreeSet<String>()
        val composerSet = TreeSet<String>()
        val genreSet = TreeSet<String>()
        val lyricistSet = TreeSet<String>()
        do {
            try {
                val file = File(cursor.getString(dataIndex))
                if (!file.exists()) throw FileNotFoundException()
                val songMetadata = mExtractor.getSongMetadata(file.path)
                Log.e("ModifiedDate", "${cursor.getString(dateModifiedIndex).toLong()}")
                val song = Song(
                    location = file.path,
                    title = cursor.getString(titleIndex),
                    album = cursor.getString(albumIndex).trim(),
                    size = cursor.getFloat(sizeIndex).toMBfromB(),
                    addedDate = cursor.getString(dateAddedIndex).toLong().formatToDate(),
                    modifiedDate = cursor.getString(dateModifiedIndex).toLong().formatToDate(),
                    artist = songMetadata.artist.trim(),
                    albumArtist = songMetadata.albumArtist.trim(),
                    composer = songMetadata.composer.trim(),
                    genre = songMetadata.genre.trim(),
                    lyricist = songMetadata.lyricist.trim(),
                    year = songMetadata.year,
                    durationMillis = songMetadata.duration,
                    durationFormatted = songMetadata.duration.toMS(),
                    mimeType = songMetadata.mimeType,
                    artUri = "content://media/external/audio/media/${cursor.getLong(songIdIndex)}/albumart"
                )
                songs.add(song)
                artistSet.add(song.artist)
                albumArtistSet.add(song.albumArtist)
                composerSet.add(song.composer)
                lyricistSet.add(song.lyricist)
                genreSet.add(song.genre)
                if (albumArtMap[song.album] == null) {
                    albumArtMap[song.album] =
                        ContentUris.withAppendedId(songCover, cursor.getLong(albumIdIndex))
                            .toString()
                }
            } catch (e: Exception) {
//                Timber.e(e.message ?: e.localizedMessage ?: "FILE_DOES_NOT_EXIST")
            }
            parsedSongs++
            _scanStatus.send(ScanStatus.ScanProgress(parsedSongs, totalSongs))
        } while (cursor.moveToNext())
        cursor.close()
        albumDao.insertAllAlbums(albumArtMap.entries.map { (t, u) -> Album(t, u) })
        artistDao.insertAllArtists(artistSet.map { Artist(it) })
        albumArtistDao.insertAllAlbumArtists(albumArtistSet.map { AlbumArtist(it) })
        composerDao.insertAllComposers(composerSet.map { Composer(it) })
        lyricistDao.insertAllLyricists(lyricistSet.map { Lyricist(it) })
        genreDao.insertAllGenres(genreSet.map { Genre(it) })
        songDao.insertAllSongs(songs)
        _scanStatus.send(ScanStatus.ScanComplete)

        songs.forEachIndexed { index, song ->
            Log.e("Song", "$song")
        }
    }
}