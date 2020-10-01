package com.ravisharma.playbackmusic.activities.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.database.model.LastPlayed
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LastAndMostPlayedViewModel : ViewModel() {

    private var lastPlayedList: MutableLiveData<List<LastPlayed>> = MutableLiveData()
    private var mostPlayedList: MutableLiveData<List<MostPlayed>> = MutableLiveData()

    fun getLastPlayedSongsList(context: Context): MutableLiveData<List<LastPlayed>> {
        val lastPlayedRepository = LastPlayedRepository(context)

        viewModelScope.launch {
            lastPlayedList.value = withContext(Dispatchers.IO) {
                lastPlayedRepository.getLastPlayedSongsList()
            }
        }
        return lastPlayedList
    }

    fun getMostPlayedSongsList(context: Context): MutableLiveData<List<MostPlayed>> {
        val mostPlayedRepository = MostPlayedRepository(context)

        viewModelScope.launch {
            mostPlayedList.value = withContext(Dispatchers.IO) {
                mostPlayedRepository.getMostPlayedSongs()
            }
        }
        return mostPlayedList
    }
}