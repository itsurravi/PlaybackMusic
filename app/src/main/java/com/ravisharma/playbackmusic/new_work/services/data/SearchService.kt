package com.ravisharma.playbackmusic.new_work.services.data

import com.ravisharma.playbackmusic.data.db.dao.AlbumArtistDao
import com.ravisharma.playbackmusic.data.db.dao.AlbumDao
import com.ravisharma.playbackmusic.data.db.dao.ArtistDao
import com.ravisharma.playbackmusic.data.db.dao.ComposerDao
import com.ravisharma.playbackmusic.data.db.dao.GenreDao
import com.ravisharma.playbackmusic.data.db.dao.LyricistDao
import com.ravisharma.playbackmusic.data.db.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.db.dao.SongDao
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.data.db.model.tables.AlbumArtist
import com.ravisharma.playbackmusic.data.db.model.tables.Artist
import com.ravisharma.playbackmusic.data.db.model.tables.Composer
import com.ravisharma.playbackmusic.data.db.model.tables.Genre
import com.ravisharma.playbackmusic.data.db.model.tables.Lyricist
import com.ravisharma.playbackmusic.data.db.model.tables.Playlist
import com.ravisharma.playbackmusic.data.db.model.tables.Song


interface SearchService {
    suspend fun searchSongs(query: String): List<Song>
    suspend fun searchAlbums(query: String): List<Album>
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchAlbumArtists(query: String): List<AlbumArtist>
    suspend fun searchComposers(query: String): List<Composer>
    suspend fun searchLyricists(query: String): List<Lyricist>
    suspend fun searchPlaylists(query: String): List<Playlist>
    suspend fun searchGenres(query: String): List<Genre>
}

class SearchServiceImpl(
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val albumArtistDao: AlbumArtistDao,
    private val composerDao: ComposerDao,
    private val lyricistDao: LyricistDao,
    private val genreDao: GenreDao,
    private val playlistDao: PlaylistDao,
): SearchService {
    override suspend fun searchSongs(query: String): List<Song> {
        return songDao.searchSongs(query)
    }

    override suspend fun searchAlbums(query: String): List<Album> {
        return albumDao.searchAlbums(query)
    }

    override suspend fun searchArtists(query: String): List<Artist> {
        return artistDao.searchArtists(query)
    }

    override suspend fun searchAlbumArtists(query: String): List<AlbumArtist> {
        return albumArtistDao.searchAlbumArtists(query)
    }

    override suspend fun searchComposers(query: String): List<Composer> {
        return composerDao.searchComposers(query)
    }

    override suspend fun searchLyricists(query: String): List<Lyricist> {
        return lyricistDao.searchLyricists(query)
    }

    override suspend fun searchPlaylists(query: String): List<Playlist> {
        return playlistDao.searchPlaylists(query)
    }

    override suspend fun searchGenres(query: String): List<Genre> {
        return genreDao.searchGenres(query)
    }
}