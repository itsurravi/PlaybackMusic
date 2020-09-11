package com.ravisharma.playbackmusic.utils

import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ravisharma.playbackmusic.model.Song

//current playing song
var playingSong: MutableLiveData<Song> = MutableLiveData()

fun setPlayingSong(song: Song) {
    playingSong.value = song
}


//playing song position
var songPosition: MutableLiveData<Int> = MutableLiveData();

fun setSongPosition(i: Int) {
    songPosition.value = i
}

//current playing list
private var playingList: MutableLiveData<ArrayList<Song>> = MutableLiveData();

fun setPlayingList(songList: ArrayList<Song>) {
    playingList.value = songList.clone() as ArrayList<Song>
}

fun getPlayingListData(): MutableLiveData<ArrayList<Song>> {
    return playingList
}

fun removeFromPlayingList(song: Song) {
    val list = playingList.value
    list?.remove(song)
    playingList.value = list
}

fun addNextSongToPlayingList(song: Song) {
    songPosition.value?.toInt()?.let {
        val list = playingList.value
        list?.add(it + 1, song)
        playingList.value = list
    }
}

fun addSongToPlayingList(song: Song) {
    val list = playingList.value
    list?.add(song)
    playingList.value = list
}

//audio file Uri for delete
var deleteUri : Uri? = null
var swiped : Boolean = false
var moved : Boolean = false
var fileDelete : Boolean = false
