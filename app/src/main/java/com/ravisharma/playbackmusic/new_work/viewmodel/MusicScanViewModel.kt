package com.ravisharma.playbackmusic.new_work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.ScanStatus
import com.ravisharma.playbackmusic.data.provider.SongExtractor
import com.ravisharma.playbackmusic.new_work.data_migrate_old_to_new.DbDataMigrate
import com.ravisharma.playbackmusic.prefrences.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicScanViewModel @Inject constructor(
    private val songExtractor: SongExtractor,
    private val prefManager: PrefManager,
    private val playlistMigrate: DbDataMigrate
) : ViewModel() {

    val scanStatus = songExtractor.scanStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = 10_000,
            replayExpirationMillis = 0
        ),
        initialValue = ScanStatus.ScanNotRunning
    )

    fun scanForMusic() {
        songExtractor.scanForMusic()
    }

    fun setOnBoardingCompleted() {
        prefManager.putBooleanPref("onBoarding", true)
    }

    fun isOnBoardingCompleted() = prefManager.getBooleanPref("onBoarding")

    fun migratePlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistMigrate.migrate()
        }
    }
}