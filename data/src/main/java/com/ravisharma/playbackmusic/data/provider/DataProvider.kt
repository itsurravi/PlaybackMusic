package com.ravisharma.playbackmusic.data.provider

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistExceptId
import com.ravisharma.playbackmusic.data.db.model.tables.PlaylistSongCrossRef
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DataProvider(
    private val context: Context,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val albumArtistDao: AlbumArtistDao,
    private val composerDao: ComposerDao,
    private val lyricistDao: LyricistDao,
    private val genreDao: GenreDao,
    private val playlistDao: PlaylistDao
) {
    val allSongs = songDao.getAllSongs()
    val allAlbums = albumDao.getAllAlbums()
    val allArtistWithSongCount = songDao.getAllArtistsWithSongCount()
    val allAlbumArtistWithSongCount = songDao.getAllAlbumArtistsWithSongCount()
    val allComposerWithSongCount = songDao.getAllComposersWithSongCount()
    val allLyricistWithSongCount = songDao.getAllLyricistsWithSongCount()
    val allPlaylistsWithSongCount = playlistDao.getAllPlaylistWithSongCount()
    val allGenresWithSongCount = songDao.getAllGenresWithSongCount()

    fun getPlaylistWithSongsById(id: Long) = playlistDao.getPlaylistWithSongs(id)
    fun getAlbumWithSongsByName(albumName: String) = albumDao.getAlbumWithSongsByName(albumName)
    fun getArtistWithSongsByName(artistName: String) =
        artistDao.getArtistWithSongsByName(artistName)

    fun getAlbumArtistWithSings(albumArtistName: String) =
        albumArtistDao.getAlbumArtistWithSongs(albumArtistName)

    fun getComposerWithSongs(composerName: String) = composerDao.getComposerWithSongs(composerName)
    fun getLyricistWithSongs(lyricistName: String) = lyricistDao.getLyricistWithSongs(lyricistName)
    fun getGenreWithSongs(genreName: String) = genreDao.getGenreWithSongs(genreName)
    fun getFavourites() = songDao.getAllFavourites()

    suspend fun searchSongs(query: String) = songDao.searchSongs(query)
    suspend fun searchAlbums(query: String) = albumDao.searchAlbums(query)
    suspend fun searchArtists(query: String) = artistDao.searchArtists(query)
    suspend fun searchAlbumArtists(query: String) = albumArtistDao.searchAlbumArtists(query)
    suspend fun searchComposers(query: String) = composerDao.searchComposers(query)
    suspend fun searchLyricists(query: String) = lyricistDao.searchLyricists(query)
    suspend fun searchPlaylists(query: String) = playlistDao.searchPlaylists(query)
    suspend fun searchGenres(query: String) = genreDao.searchGenres(query)

    suspend fun createPlaylist(playlistName: String, showToast: (String) -> Unit) {
        if (playlistName.trim().isEmpty()) return
        val playlist = PlaylistExceptId(
            playlistName = playlistName.trim(),
            createdAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        showToast("Playlist $playlistName created")
    }

    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)
    suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song)
    }

    suspend fun insertPlaylistSongCrossRefs(playlistSongCrossRefs: List<PlaylistSongCrossRef>) =
        playlistDao.insertPlaylistSongCrossRef(playlistSongCrossRefs)

    suspend fun deletePlaylistSongCrossRef(playlistSongCrossRef: PlaylistSongCrossRef) =
        playlistDao.deletePlaylistSongCrossRef(playlistSongCrossRef)

    private var callback: Callback? = null

    private val _queue = mutableListOf<Song>()
    val queue: List<Song> = _queue

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    fun moveItem(fromIndex: Int, toIndex: Int) {
        _queue.apply { add(toIndex, removeAt(fromIndex)) }
    }

    suspend fun updateSong(song: Song) {
        if (_currentSong.value?.location == song.location) {
            _currentSong.update { song }
            callback?.updateNotification()
        }
        for (idx in _queue.indices) {
            if (_queue[idx].location == song.location) {
                _queue[idx] = song
                break
            }
        }
        songDao.updateSong(song)
    }

    private var remIdx = 0

    @Synchronized
    fun setQueue(newQueue: List<Song>, startPlayingFromIndex: Int) {
//        if (newQueue.isEmpty()) return
//        _queue.apply {
//            clear()
//            addAll(newQueue)
//        }
//        _currentSong.value = newQueue[startPlayingFromIndex]
//        if (callback == null) {
//            val intent = Intent(context, ZenPlayer::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
//                context.startService(intent)
//            }
//            remIdx = startPlayingFromIndex
//        } else {
//            callback?.setQueue(newQueue, startPlayingFromIndex)
//        }
    }

    /**
     * Returns true if added to queue else returns false if already in queue
     */
    @Synchronized
    fun addToQueue(song: Song): Boolean {
        if (_queue.any { it.location == song.location }) return false
        _queue.add(song)
        callback?.addToQueue(song)
        return true
    }

    fun setPlayerRunning(callback: Callback) {
        this.callback = callback
        this.callback?.setQueue(_queue, remIdx)
    }

    fun updateCurrentSong(currentSongIndex: Int) {
        if (currentSongIndex < 0 || currentSongIndex >= _queue.size) return
        _currentSong.update { _queue[currentSongIndex] }
    }

    fun getSongAtIndex(index: Int): Song? {
        if (index < 0 || index >= _queue.size) return null
        return _queue[index]
    }

    fun stopPlayerRunning() {
        this.callback = null
        _currentSong.update { null }
        _queue.clear()
    }

    interface Callback {
        fun setQueue(newQueue: List<Song>, startPlayingFromIndex: Int)
        fun addToQueue(song: Song)
        fun updateNotification()
    }
}