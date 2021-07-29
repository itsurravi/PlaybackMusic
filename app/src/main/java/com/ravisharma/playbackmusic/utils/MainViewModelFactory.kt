package com.ravisharma.playbackmusic.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ravisharma.playbackmusic.MainActivityViewModel
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.prefrences.TinyDB
import java.lang.IllegalArgumentException

class MainViewModelFactory(
    private val repository: PlaylistRepository,
    private val tinyDB: TinyDB
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(repository, tinyDB) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
}