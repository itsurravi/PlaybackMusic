package com.ravisharma.playbackmusic.data.provider

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao
import com.ravisharma.playbackmusic.data.db.model.MiniSong
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.data.db.model.tables.AlbumArtist
import com.ravisharma.playbackmusic.data.db.model.tables.Artist
import com.ravisharma.playbackmusic.data.db.model.tables.Composer
import com.ravisharma.playbackmusic.data.db.model.tables.Genre
import com.ravisharma.playbackmusic.data.db.model.tables.Lyricist
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.utils.formatToDate
import com.ravisharma.playbackmusic.data.utils.toMBfromB
import com.ravisharma.playbackmusic.data.utils.toMS
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.util.TreeMap
import java.util.concurrent.atomic.AtomicInteger

class SongExtractor(
    private val scope: CoroutineScope,
    private val context: Context,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val albumArtistDao: AlbumArtistDao,
    private val composerDao: ComposerDao,
    private val lyricistDao: LyricistDao,
    private val genreDao: GenreDao,
) {

    init {
        cleanData()
    }

    fun cleanData() {
        scope.launch {
            val jobs = mutableListOf<Job>()
            songDao.getSongs().forEach {
                try {
                    if (!File(it.location).exists()) {
                        jobs += launch { songDao.deleteSong(it) }
                    }
                } catch (_: Exception) {

                }
            }
            jobs.joinAll()
            jobs.clear()
            albumDao.cleanAlbumTable()
            artistDao.cleanArtistTable()
            albumArtistDao.cleanAlbumArtistTable()
            composerDao.cleanComposerTable()
            lyricistDao.cleanLyricistTable()
            genreDao.cleanGenreTable()
        }
    }

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DATE_MODIFIED,
    )

    private val _scanStatus = Channel<ScanStatus>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val scanStatus = _scanStatus.receiveAsFlow()

    fun scanForMusic() {
        scope.launch {
            _scanStatus.send(ScanStatus.ScanStarted)
            val (songs, albums) = extract(
                statusListener = { parsed, total ->
                    _scanStatus.trySend(ScanStatus.ScanProgress(parsed, total))
                }
            )
            val insertJobs = listOf(
                launch {
                    val artists = songs.map { it.artist }.toSet().map { Artist(it) }
                    artistDao.insertAllArtists(artists)
                },
                launch {
                    val albumArtists = songs.map { it.albumArtist }.toSet().map { AlbumArtist(it) }
                    albumArtistDao.insertAllAlbumArtists(albumArtists)
                },
                launch {
                    val lyricists = songs.map { it.lyricist }.toSet().map { Lyricist(it) }
                    lyricistDao.insertAllLyricists(lyricists)
                },
                launch {
                    val composers = songs.map { it.composer }.toSet().map { Composer(it) }
                    composerDao.insertAllComposers(composers)
                },
                launch {
                    val genres = songs.map { it.genre }.toSet().map { Genre(it) }
                    genreDao.insertAllGenres(genres)
                }
            )
            albumDao.insertAllAlbums(albums)
            insertJobs.joinAll()
            songDao.insertAllSongs(songs)
            _scanStatus.send(ScanStatus.ScanComplete)
        }
    }

    private suspend fun extract(
        statusListener: ((parsed: Int, total: Int) -> Unit)? = null
    ): Pair<List<Song>, List<Album>> {
        val selection = StringBuilder()
        val selectionArgs = arrayListOf<String>()
        selection.append(MediaStore.Audio.Media.IS_MUSIC + " != 0 ").append("AND ")
        selection.append(MediaStore.Audio.Media.DATA + " NOT LIKE '%Record%' ")

        val query = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val cursor = context.contentResolver.query(
            query,
            projection,
            selection.toString(),
            selectionArgs.toTypedArray(),
            "upper(" + MediaStore.Audio.Media.TITLE + ") ASC",
            null
        ) ?: return Pair(emptyList(), emptyList())

        val songCover = Uri.parse("content://media/external/audio/albumart")
        val albumArtMap = TreeMap<String, Long>()
        val dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val dateAddedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
        val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
        val songIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val dSongs = ArrayList<Deferred<Song?>>()
        val total = cursor.count
        val parsed = AtomicInteger(0)
        val parseCompletionHandler = object : CompletionHandler {
            override fun invoke(cause: Throwable?) {
                statusListener?.invoke(parsed.incrementAndGet(), total)
            }
        }
        cursor.moveToFirst()
        do {
            try {
                val songPath = cursor.getString(dataIndex)
                val songFile = File(songPath)
                if (!songFile.exists()) throw FileNotFoundException()

                val size = cursor.getString(sizeIndex)
                val addedDate = cursor.getString(dateAddedIndex)
                val modifiedDate = cursor.getString(dateModifiedIndex)
                val songId = cursor.getLong(songIdIndex)
                val title = cursor.getString(titleIndex).trim()
                val album = cursor.getString(albumIndex).trim()
                albumArtMap[album] = cursor.getLong(albumIdIndex)
                dSongs.add(
                    scope.async {
                        getSong(
                            path = songPath,
                            size = size,
                            addedDate = addedDate,
                            modifiedDate = modifiedDate,
                            songId = songId,
                            title = title,
                            album = album,
                        )
                    }.apply {
                        invokeOnCompletion(parseCompletionHandler)
                    }
                )
            } catch (_: Exception) {

            }
        } while (cursor.moveToNext())
        val songs = dSongs.awaitAll().filterNotNull()
        cursor.close()
        val albums = albumArtMap.map { (t, u) ->
            Album(
                t,
                ContentUris.withAppendedId(songCover, u).toString()
            )
        }
        return Pair(songs, albums)
    }

    companion object {
        private const val UNKNOWN = "Unknown"
    }

    private fun getSong(
        path: String,
        size: String,
        addedDate: String,
        modifiedDate: String,
        songId: Long,
        title: String,
        album: String,
    ): Song? {
        val extractor = MediaMetadataRetriever()
        var result: Song? = null
        try {
            extractor.setDataSource(path)
            val durationMillis =
                extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0
            val sampleRate = if (Build.VERSION.SDK_INT >= 31) {
                extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                    ?.toFloatOrNull() ?: 0f
            } else 0f
            val bitsPerSample = if (Build.VERSION.SDK_INT >= 31) {
                extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
                    ?.toIntOrNull() ?: 0
            } else 0
            val song = Song(
                location = path,
                title = title,
                album = album,
                size = size.toFloat().toMBfromB(),
                addedDate = addedDate.toLong().formatToDate(),
                modifiedDate = modifiedDate.toLong().formatToDate(),
                artUri = "content://media/external/audio/media/$songId/albumart",
                artist = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?.trim() ?: UNKNOWN,
                albumArtist = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
                    ?.trim() ?: UNKNOWN,
                composer = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
                    ?.trim() ?: UNKNOWN,
                genre = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.trim()
                    ?: UNKNOWN,
                lyricist = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER)
                    ?.trim() ?: UNKNOWN,
                year = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?.toIntOrNull() ?: 0,
                comment = null,
                durationMillis = durationMillis,
                durationFormatted = durationMillis.toMS(),
                bitrate = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toFloatOrNull() ?: 0f,
                sampleRate = sampleRate,
                bitsPerSample = bitsPerSample,
                mimeType = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                favourite = false,
            )
            result = song
        } catch (e: Exception) {
            result = null
        } finally {
            try {
                extractor.release()
            } catch (_: Exception) {
            }
        }
        return result
    }


}