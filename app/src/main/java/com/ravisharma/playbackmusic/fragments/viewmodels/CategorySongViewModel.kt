/*
package com.ravisharma.playbackmusic.fragments.viewmodels

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.model.LastPlayed
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CategorySongViewModel @Inject constructor(
    private val lastPlayedRepository: LastPlayedRepository,
    private val mostPlayedRepository: MostPlayedRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private var songsList: MutableLiveData<ArrayList<Song>> = MutableLiveData()

    private var lastPlayedList: LiveData<List<LastPlayed>> = MutableLiveData()
    private var mostPlayedList: LiveData<List<MostPlayed>> = MutableLiveData()

    fun getCategorySongs(
        queryType: String,
        albumId: String,
        contentResolver: ContentResolver
    ): MutableLiveData<ArrayList<Song>> {
        viewModelScope.launch(Dispatchers.Main) {
            songsList.value = withContext(Dispatchers.IO) {
                getSongs(queryType, albumId, contentResolver)
            }
        }
        return songsList
    }

    private fun getSongs(
        queryType: String,
        id: String,
        contentResolver: ContentResolver
    ): ArrayList<Song> {
        val songList = ArrayList<Song>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var musicCursor: Cursor? = null

        if (queryType == "Album") {
            musicCursor = contentResolver.query(
                musicUri,
                null,
                MediaStore.Audio.Media.ALBUM_ID + "=" + id*/
/* + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ", arrayOf("%Record%")*//*
,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
            )
        } else if (queryType == "Artist") {
            musicCursor = contentResolver.query(
                musicUri,
                null,
                MediaStore.Audio.Media.ARTIST_ID + "=" + id + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ",
                arrayOf("%Record%"),
                MediaStore.Audio.Media.TITLE + " ASC"
            )
        }

        musicCursor?.let { cursor ->
            if (cursor.count > 0) {

                cursor.moveToFirst()

                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val composerColumn = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
                val pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dateModifyColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)

                //add songs to list
                do {
                    val thisId = cursor.getLong(idColumn)
                    val thisTitle = cursor.getString(titleColumn)
                    val thisArtist = cursor.getString(artistColumn)
                    val thisAlbum = cursor.getString(albumColumn)
                    val thisComposer = cursor.getString(composerColumn)
                    val thisPath = cursor.getString(pathColumn)
                    val thisDateModify = cursor.getString(dateModifyColumn)
                    val thisDuration = cursor.getLong(durationColumn)
                    val thisAlbumAid = cursor.getLong(albumIdColumn)
                    val art_content = Uri.parse("content://media/external/audio/albumart")
                    val albumArt = ContentUris.withAppendedId(art_content, thisAlbumAid)

                    songList.add(
                        Song(
                            thisId,
                            thisTitle,
                            thisArtist,
                            thisPath,
                            thisDateModify,
                            albumArt.toString(),
                            thisDuration,
                            thisAlbum,
                            thisComposer
                        )
                    )
                } while (cursor.moveToNext())
                cursor.close()
            }
            musicCursor.close()
        }
        return songList
    }

    fun getLastPlayedSongsList(): LiveData<List<LastPlayed>> {
        viewModelScope.launch {
            lastPlayedList = lastPlayedRepository.getLastPlayedSongsList()
        }
        return lastPlayedList
    }

    fun getMostPlayedSongsList(): LiveData<List<MostPlayed>> {
        viewModelScope.launch {
            mostPlayedList = mostPlayedRepository.getMostPlayedSongs()
        }
        return mostPlayedList
    }

    fun getPlaylistSong(actName: String): LiveData<List<Playlist>> {
        return playlistRepository.getPlaylistSong(actName)
    }

    fun removeSong(playlistName: String?, songId: Long) {
        playlistRepository.removeSong(playlistName, songId)
    }
}*/
