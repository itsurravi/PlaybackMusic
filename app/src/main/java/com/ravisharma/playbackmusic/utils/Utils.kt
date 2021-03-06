package com.ravisharma.playbackmusic.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.ravisharma.playbackmusic.model.Song

//current playing song
var curPlayingSong: MutableLiveData<Song> = MutableLiveData()

fun setPlayingSong(song: Song) {
    curPlayingSong.value = song
}


//playing song position
var curPlayingSongPosition: MutableLiveData<Int> = MutableLiveData()

fun setSongPosition(i: Int) {
    curPlayingSongPosition.value = i
}

//current playing list
private var curPlayingList: MutableLiveData<ArrayList<Song>> = MutableLiveData()

fun setPlayingList(songList: ArrayList<Song>) {
    curPlayingList.value = songList.clone() as ArrayList<Song>
}

fun getPlayingListData(): MutableLiveData<ArrayList<Song>> {
    return curPlayingList
}

fun removeFromPlayingList(song: Song) {
    val list = curPlayingList.value
    list?.remove(song)
    curPlayingList.value = list
}

fun addNextSongToPlayingList(song: Song) {
    curPlayingSongPosition.value?.toInt()?.let {
        val list = curPlayingList.value
        list?.add(it + 1, song)
        curPlayingList.value = list
    }
}

fun addSongToPlayingList(song: Song) {
    val list = curPlayingList.value
    list?.add(song)
    curPlayingList.value = list
}

//audio file Uri for delete
var deleteUri : Uri? = null
var swiped : Boolean = false
var moved : Boolean = false
var fileDelete : Boolean = false
