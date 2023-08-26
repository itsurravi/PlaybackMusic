package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.ActivityCategorySongBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.TracksAdapter
import com.ravisharma.playbackmusic.new_work.ui.extensions.LongItemClick
import com.ravisharma.playbackmusic.new_work.ui.extensions.onSongLongPress
import com.ravisharma.playbackmusic.new_work.ui.extensions.shareSong
import com.ravisharma.playbackmusic.new_work.ui.extensions.showSongInfo
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CollectionListingFragment : Fragment(R.layout.activity_category_song) {

    private var _binding: ActivityCategorySongBinding? = null
    private val binding get() = _binding!!

    private val collectionViewModel: CollectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val collectionType: CollectionType? = it.getParcelable(CollectionType.Category)

            collectionType?.let {
                collectionViewModel.loadCollection(collectionType)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityCategorySongBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            binding.playlistLayout.apply {
                firstLayout.isVisible = true

                songList.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    itemAnimator = DefaultItemAnimator()
                    addItemDecoration(
                        DividerItemDecoration(
                            this.context,
                            DividerItemDecoration.VERTICAL
                        )
                    )
                    adapter = TracksAdapter(
                        onItemClick = ::songClicked,
                        onItemLongClick = ::songLongClicked
                    )
                }

                imageBack1.setOnClickListener {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            collectionViewModel.collectionUi.collect {
                updateUi(it)
            }
        }
    }

    private fun updateUi(data: CollectionUi) {
        binding.playlistLayout.apply {
            txtPlaylistName1.text = data.topBarTitle
            noOfSongs.text = "${data.songs.size}"

            albumArt.load(data.topBarBackgroundImageUri) {
                error(R.drawable.logo)
                crossfade(true)
            }

            if(data.songs.isEmpty()) {
                noDataFound.noDataLayout.isVisible = true
            } else {
                noDataFound.noDataLayout.isVisible = false
                (songList.adapter as TracksAdapter).submitList(data.songs)
            }
        }
    }

    private fun songClicked(song: Song, position: Int) {
        val currentList =
            (binding.playlistLayout.songList.adapter as TracksAdapter).getCurrentList()
        collectionViewModel.setQueue(currentList, position)
    }

    private fun songLongClicked(song: Song, position: Int) {
        requireContext().onSongLongPress(song) { longItemClick ->
            when (longItemClick) {
                LongItemClick.Play -> {
                    songClicked(song, position)
                }

                LongItemClick.SinglePlay -> {
                    collectionViewModel.setQueue(listOf(song), 0)
                }

                LongItemClick.PlayNext -> {
                    collectionViewModel.addNextInQueue(song)
                }

                LongItemClick.AddToQueue -> {
                    collectionViewModel.addToQueue(song)
                }

                LongItemClick.AddToPlaylist -> {
                    // TODO
                }

                LongItemClick.Share -> {
                    requireContext().shareSong(song.location)
                }

                LongItemClick.Details -> {
                    requireContext().showSongInfo(song)
                }
            }
        }
        requireContext().showToast("song long click")
    }
}