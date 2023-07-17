package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import androidx.lifecycle.ViewModel
import com.ravisharma.playbackmusic.data.provider.DataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataProvider: DataProvider
): ViewModel() {

    val allSongs = dataProvider.allSongs
    val allAlbums = dataProvider.allAlbums
    val allArtists = dataProvider.allArtistWithSongCount

}