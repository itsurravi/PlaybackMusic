package com.ravisharma.playbackmusic.data.components

class GetAll(
    private val daoCollection: DaoCollection
) {
    fun allSongs() = daoCollection.songDao.getAllSongs()

    fun allAlbums() = daoCollection.albumDao.getAllAlbums()

    fun allArtistWithSongCount() = daoCollection.songDao.getAllArtistsWithSongCount()

    fun allAlbumArtistWithSongCount() = daoCollection.songDao.getAllAlbumArtistsWithSongCount()

    fun allComposerWithSongCount() = daoCollection.songDao.getAllComposersWithSongCount()

    fun allLyricistWithSongCount() = daoCollection.songDao.getAllLyricistsWithSongCount()

    fun allPlaylistsWithSongCount() = daoCollection.playlistDao.getAllPlaylistWithSongCount()

    fun allGenresWithSongCount() = daoCollection.songDao.getAllGenresWithSongCount()
}