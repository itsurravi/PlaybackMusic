package com.ravisharma.playbackmusic.new_work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.DataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val dataProvider: DataProvider,
) : ViewModel() {

    private val _searchResultList = MutableStateFlow<List<Song>>(emptyList())
    val searchResultList = _searchResultList.asStateFlow()

    fun searchSong(title: String) {
        viewModelScope.launch {
            _searchResultList.emit(dataProvider.querySearch.searchSongs(title))
        }
    }
}