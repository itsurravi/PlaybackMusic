package com.ravisharma.playbackmusic.provider


import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.ravisharma.playbackmusic.model.Album
import com.ravisharma.playbackmusic.model.Artist
import com.ravisharma.playbackmusic.model.Song
import kotlinx.coroutines.*

class SongsProvider {

    companion object {
        var songListByName: MutableLiveData<ArrayList<Song>> = MutableLiveData()
            private set
        var songListByDate: MutableLiveData<ArrayList<Song>> = MutableLiveData()
            private set
        var albumList: MutableLiveData<ArrayList<Album>> = MutableLiveData()
            private set
        var artistList: MutableLiveData<ArrayList<Artist>> = MutableLiveData()
            private set
    }

    private var s5: MutableLiveData<Boolean> = MutableLiveData()

    fun fetchAllData(musicResolver: ContentResolver): MutableLiveData<Boolean> {
        startDataFetch(musicResolver)
        return s5
    }

    private fun startDataFetch(musicResolver: ContentResolver) {

        CoroutineScope(Dispatchers.IO).launch {
            val data1 = async { getSongByName(musicResolver) }
            val data2 = async { getSongByDate(musicResolver) }
            val data3 = async { getAlbums(musicResolver) }
            val data4 = async { getArtists(musicResolver) }

            if (data1.await() && data2.await() && data3.await() && data4.await()) {
                s5.postValue(true)
                this.cancel()
            } else {
                s5.postValue(false)
                this.cancel()
            }
        }
    }

    private fun getSongByName(musicResolver: ContentResolver): Boolean {
        val songList = ArrayList<Song>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ", arrayOf(
                "%Record%"), "upper(" + MediaStore.Audio.Media.TITLE + ") ASC")
        musicCursor?.let {
            if (it.count > 0) {
                it.moveToFirst()
                //get columns
                val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
                val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val composerColumn = it.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
                val pathColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dateModifyColumn = it.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)


                //add songs to list
                do {
                    try {
                        val thisId = it.getLong(idColumn)
                        val thisTitle = it.getString(titleColumn)
                        val thisArtist = it.getString(artistColumn)
                        val thisAlbum = it.getString(albumColumn)
                        val thisComposer = it.getString(composerColumn)
                        val thisPath = it.getString(pathColumn)
                        val thisDateModify = it.getString(dateModifyColumn)
                        val thisDuration = it.getLong(durationColumn)
                        val thisAlbumAid = it.getLong(albumIdColumn)
                        val ART_CONTENT = Uri.parse("content://media/external/audio/albumart")
                        val albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid)
                        songList.add(Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, albumArt.toString(), thisDuration, thisAlbum, thisComposer))
                    } catch (ignored: Exception) {

                    }
                } while (it.moveToNext())

                it.close()
                songListByName.postValue(songList)
                return true
            } else {
                it.close()
                return true
            }
        }
        return false
    }

    private fun getSongByDate(musicResolver: ContentResolver): Boolean {
        val songList = ArrayList<Song>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ", arrayOf("%Record%"), MediaStore.Audio.Media.DATE_MODIFIED + " DESC")

        musicCursor?.let {
            if (it.count > 0) {
                it.moveToFirst()
                //get columns
                val idColumn: Int = it.getColumnIndex(MediaStore.Audio.Media._ID)
                val albumIdColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val titleColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val composerColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
                val pathColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dateModifyColumn: Int = it.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)


                //add songs to list
                do {
                    try {
                        val thisId = it.getLong(idColumn)
                        val thisTitle = it.getString(titleColumn)
                        val thisArtist = it.getString(artistColumn)
                        val thisAlbum = it.getString(albumColumn)
                        val thisComposer = it.getString(composerColumn)
                        val thisPath = it.getString(pathColumn)
                        val thisDateModify = it.getString(dateModifyColumn)
                        val thisDuration = it.getLong(durationColumn)
                        val thisAlbumAid = it.getLong(albumIdColumn)
                        val ART_CONTENT = Uri.parse("content://media/external/audio/albumart")
                        val albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid)
                        songList.add(Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, albumArt.toString(), thisDuration, thisAlbum, thisComposer))
                    } catch (ignored: Exception) {

                    }
                } while (it.moveToNext())

                it.close()
                songListByDate.postValue(songList)
                return true
            } else {
                it.close()
                return true
            }
        }
        return false
    }

    private fun getAlbums(musicResolver: ContentResolver): Boolean {
        val songList = ArrayList<Album>()
        val musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        val albumProjection = arrayOf(
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        val musicCursor = musicResolver.query(musicUri, albumProjection,
                null, null, MediaStore.Audio.Media.ALBUM + " ASC")

        musicCursor?.let {
            if (it.count > 0) {
                it.moveToFirst()
                //get columns
                val idColumn: Int = it.getColumnIndex(MediaStore.Audio.Albums._ID)
                val albumColumn: Int = it.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                val artistColumn: Int = it.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
                val numberOfSongs: Int = it.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)


                //add albums to list
                do {
                    try {
                        val thisId: Long = it.getLong(idColumn)
                        val thisAlbum: String = it.getString(albumColumn)
                        val thisArtist: String = it.getString(artistColumn)
                        val thisNumberOfSongs: String = it.getString(numberOfSongs)
                        var albumArt: Uri? = null
                        val thisAlbumAid: Long = it.getLong(idColumn)
                        val ART_CONTENT = Uri.parse("content://media/external/audio/albumart")
                        albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid)
                        val songs = thisNumberOfSongs.toInt()
                        if (songs > 0) {
                            songList.add(Album(thisId, albumArt, thisAlbum, thisArtist, thisNumberOfSongs))
                        }
                    } catch (ignored: Exception) {
                    }
                } while (it.moveToNext())

                it.close()
                albumList.postValue(songList)
                return true
            } else {
                it.close()
                return true
            }
        }
        return false
    }

    private fun getArtists(musicResolver: ContentResolver): Boolean {
        val songList = ArrayList<Artist>()
        val musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

        val artistProjection = arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.ARTIST_KEY
        )

        val musicCursor = musicResolver.query(musicUri, artistProjection,
                null, null, MediaStore.Audio.Media.ARTIST + " ASC")

        musicCursor?.let {
            if (it.count > 0) {
                it.moveToFirst()
                //get columns
                val idColumn: Int = it.getColumnIndex(MediaStore.Audio.Artists._ID)
                val artistColumn: Int = it.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
                val albumColumn: Int = it.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                val tracksColumn: Int = it.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)


                //add artists to list
                do {
                    try {
                        val thisId: Long = it.getLong(idColumn)
                        val thisArtist: String = it.getString(artistColumn)
                        val thisAlbum: String = it.getString(albumColumn)
                        val thisTracks: String = it.getString(tracksColumn)
                        songList.add(Artist(thisId, thisArtist, thisAlbum, thisTracks))
                    } catch (e: Exception) {

                    }
                } while (it.moveToNext())

                it.close()
                artistList.postValue(songList)

                return true
            } else {
                it.close()
                return true
            }
        }
        return false
    }
}