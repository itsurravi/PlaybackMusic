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

class SearchViewModel : ViewModel() {

    private var searchLiveData: MutableLiveData<List<Song>> = MutableLiveData()

    fun getSearchList(): MutableLiveData<List<Song>> {
        return searchLiveData
    }

    private fun performSearch(searchName: String, contentResolver: ContentResolver): List<Song> {
        val songList: ArrayList<Song> = ArrayList()
        val musicResolver: ContentResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        try {
            val musicCursor = musicResolver.query(musicUri, null,
                    MediaStore.Audio.Media.TITLE + " LIKE ? OR " +
                            MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ? OR " +
                            MediaStore.Audio.Media.ALBUM + " LIKE ? OR " +
                            MediaStore.Audio.Media.ARTIST + " LIKE ?",
                    arrayOf("%$searchName%","%$searchName%","%$searchName%","%$searchName%"),
                    MediaStore.Audio.Media.TITLE + " ASC")

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
        } catch (ignored: Exception) {

        }

        return songList
    }

    fun search(searchName: String, contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.Main) {
            searchLiveData.value = withContext(Dispatchers.IO) {
                performSearch(searchName, contentResolver)
            }
        }
    }
}