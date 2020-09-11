package com.ravisharma.playbackmusic.activities.viewmodel

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ArtistSongViewModel : ViewModel() {
    private var artistSong: MutableLiveData<ArrayList<Song>> = MutableLiveData()

    fun getArtistSongs(artistId: String, contentResolver: ContentResolver): MutableLiveData<ArrayList<Song>> {
        viewModelScope.launch(Dispatchers.Main) {
            artistSong.value = withContext(Dispatchers.IO) {
                getArtistSongsList(artistId, contentResolver)
            }
        }
        return artistSong
    }

    private fun getArtistSongsList(artistId: String, contentResolver: ContentResolver): ArrayList<Song> {
        val songList = java.util.ArrayList<Song>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = contentResolver.query(musicUri, null,
                MediaStore.Audio.Media.ARTIST_ID + "=" + artistId + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ", arrayOf("%Record%"), MediaStore.Audio.Media.TITLE + " ASC")
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
            val pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)

            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val thisAlbum = musicCursor.getString(albumColumn)
                val thisComposer = musicCursor.getString(composerColumn)
                val thisPath = musicCursor.getString(pathColumn)
                val thisDateModify = musicCursor.getString(dateModifyColumn)
                val thisDuration = musicCursor.getLong(durationColumn)
                val thisAlbumAid = musicCursor.getLong(albumIdColumn)
                val ART_CONTENT = Uri.parse("content://media/external/audio/albumart")
                val albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid)
                songList.add(Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, albumArt.toString(), thisDuration, thisAlbum, thisComposer))
            } while (musicCursor.moveToNext())
        }
        return songList
    }
}