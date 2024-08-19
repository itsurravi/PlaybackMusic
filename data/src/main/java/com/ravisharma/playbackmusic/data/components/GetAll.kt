package com.ravisharma.playbackmusic.data.components

class GetAll(
    private val daoCollection: DaoCollection
) {
    fun songs() = daoCollection.songDao.getAllSongs()

    fun albums() = daoCollection.albumDao.getAllAlbums()

    fun artists() = daoCollection.songDao.getAllArtistsWithSongCount()

    fun albumArtists() = daoCollection.songDao.getAllAlbumArtistsWithSongCount()

    fun playlists() = daoCollection.playlistDao.getAllPlaylistWithSongCount()

    fun composers() = daoCollection.songDao.getAllComposersWithSongCount()

    fun lyricists() = daoCollection.songDao.getAllLyricistsWithSongCount()

    fun genres() = daoCollection.songDao.getAllGenresWithSongCount()
}