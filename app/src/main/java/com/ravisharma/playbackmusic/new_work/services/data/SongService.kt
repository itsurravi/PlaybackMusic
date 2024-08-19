package com.ravisharma.playbackmusic.new_work.services.data

import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao
import com.ravisharma.playbackmusic.data.db.model.AlbumArtistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.ComposerWithSongCount
import com.ravisharma.playbackmusic.data.db.model.GenreWithSongCount
import com.ravisharma.playbackmusic.data.db.model.LyricistWithSongCount
import com.ravisharma.playbackmusic.data.db.model.embedded.AlbumArtistWithSongs
import com.ravisharma.playbackmusic.data.db.model.embedded.AlbumWithSongs
import com.ravisharma.playbackmusic.data.db.model.embedded.ArtistWithSongs
import com.ravisharma.playbackmusic.data.db.model.embedded.ComposerWithSongs
import com.ravisharma.playbackmusic.data.db.model.embedded.GenreWithSongs
import com.ravisharma.playbackmusic.data.db.model.embedded.LyricistWithSongs
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import kotlinx.coroutines.flow.Flow

interface SongService {
    val songs: Flow<List<Song>>
    val albums: Flow<List<Album>>
    val artists: Flow<List<ArtistWithSongCount>>
    val albumArtists: Flow<List<AlbumArtistWithSongCount>>
    val composers: Flow<List<ComposerWithSongCount>>
    val lyricists: Flow<List<LyricistWithSongCount>>
    val genres: Flow<List<GenreWithSongCount>>

    fun getRecentAddedSongs(): Flow<List<Song>>
    fun getMostPlayedSongs(): Flow<List<Song>>
    fun getAlbumWithSongsByName(albumName: String): Flow<AlbumWithSongs?>
    fun getArtistWithSongsByName(artistName: String): Flow<ArtistWithSongs?>
    fun getAlbumArtistWithSongsByName(albumArtistName: String): Flow<AlbumArtistWithSongs?>
    fun getComposerWithSongsByName(composerName: String): Flow<ComposerWithSongs?>
    fun getLyricistWithSongsByName(lyricistName: String): Flow<LyricistWithSongs?>
    fun getGenreWithSongsByName(genre: String): Flow<GenreWithSongs?>

    fun getFavouriteSongs(): Flow<List<Song>>

    suspend fun updateSong(song: Song)
    suspend fun getSongsFromLocations(locations: List<String>): List<Song>
}

class SongServiceImpl(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val albumArtistDao: AlbumArtistDao,
    private val composerDao: ComposerDao,
    private val lyricistDao: LyricistDao,
    private val genreDao: GenreDao,
) : SongService {
    override val songs: Flow<List<Song>> = songDao.getAllSongs()

    override val albums: Flow<List<Album>> = albumDao.getAllAlbums()

    override val artists: Flow<List<ArtistWithSongCount>> = songDao.getAllArtistsWithSongCount()

    override val albumArtists: Flow<List<AlbumArtistWithSongCount>> =
        songDao.getAllAlbumArtistsWithSongCount()

    override val composers: Flow<List<ComposerWithSongCount>> =
        songDao.getAllComposersWithSongCount()

    override val lyricists: Flow<List<LyricistWithSongCount>> =
        songDao.getAllLyricistsWithSongCount()

    override val genres: Flow<List<GenreWithSongCount>> = songDao.getAllGenresWithSongCount()

    override fun getRecentAddedSongs(): Flow<List<Song>> {
        return songDao.getRecentAddedSongs()
    }

    override fun getMostPlayedSongs(): Flow<List<Song>> {
        return songDao.getMostPlayedSongs()
    }

    override fun getAlbumWithSongsByName(albumName: String): Flow<AlbumWithSongs?> {
        return albumDao.getAlbumWithSongsByName(albumName)
    }

    override fun getArtistWithSongsByName(artistName: String): Flow<ArtistWithSongs?> {
        return artistDao.getArtistWithSongsByName(artistName)
    }

    override fun getAlbumArtistWithSongsByName(albumArtistName: String): Flow<AlbumArtistWithSongs?> {
        return albumArtistDao.getAlbumArtistWithSongs(albumArtistName)
    }

    override fun getComposerWithSongsByName(composerName: String): Flow<ComposerWithSongs?> {
        return composerDao.getComposerWithSongs(composerName)
    }

    override fun getLyricistWithSongsByName(lyricistName: String): Flow<LyricistWithSongs?> {
        return lyricistDao.getLyricistWithSongs(lyricistName)
    }

    override fun getGenreWithSongsByName(genre: String): Flow<GenreWithSongs?> {
        return genreDao.getGenreWithSongs(genre)
    }

    override fun getFavouriteSongs(): Flow<List<Song>> {
        return songDao.getAllFavourites()
    }

    override suspend fun updateSong(song: Song) {
        songDao.updateSong(song)
    }

    override suspend fun getSongsFromLocations(locations: List<String>): List<Song> {
        return songDao.getSongsFromLocations(locations)
    }
}