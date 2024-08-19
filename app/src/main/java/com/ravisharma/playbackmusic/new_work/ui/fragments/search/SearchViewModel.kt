package com.ravisharma.playbackmusic.new_work.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravisharma.playbackmusic.new_work.services.data.PlayerService
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.SearchService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchService: SearchService,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val searchResult = _query.map { query ->
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            emptyList()
        } else {
            searchService.searchSongs(trimmedQuery)
        }
    }.catch { exception ->
        exception.printStackTrace()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun updateQuery(query: String) {
        _query.update { query }
    }
}