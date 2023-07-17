package com.ravisharma.playbackmusic.new_work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.data.provider.DataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicScanViewModel @Inject constructor(
    private val dataProvider: DataProvider
) : ViewModel() {

    val allSongs = dataProvider.getAll.songs()

    val scanStatus = dataProvider.scanStatus.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 300,
            replayExpirationMillis = 0
        ),
        initialValue = ScanStatus.ScanNotRunning
    )

    fun scanForMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            dataProvider.performMusicScan()
        }
    }
}