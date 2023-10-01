package com.ravisharma.playbackmusic.new_work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.prefrences.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicScanViewModel @Inject constructor(
    private val dataProvider: DataProvider,
    private val prefManager: PrefManager
) : ViewModel() {

    val scanStatus = dataProvider.scanStatus.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 300,
            replayExpirationMillis = 0
        ),
        initialValue = ScanStatus.ScanNotRunning
    )

    fun cleanData() {
        viewModelScope.launch {
            dataProvider.cleanData()
        }
    }

    fun scanForMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            dataProvider.performMusicScan()
        }
    }

    fun setOnBoardingCompleted() {
        prefManager.putBooleanPref("onBoarding", true)
    }

    fun isOnBoardingCompleted() = prefManager.getBooleanPref("onBoarding")
}