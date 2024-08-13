package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentCurrentQueueBinding
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.ui.adapters.CurrentQueueAdapter
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class CurrentQueueFragment : Fragment(R.layout.fragment_current_queue) {

    private var _binding: FragmentCurrentQueueBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCurrentQueueBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            val currentQueueAdapter = CurrentQueueAdapter(
                onItemClick = ::songClicked
            )

            rvSongList.apply {
                adapter = currentQueueAdapter
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
            }.also {
                currentQueueAdapter.submitList(mainViewModel.queue) {
                    it.scrollToPosition(mainViewModel.queue.indexOf(mainViewModel.currentSong.value))
                }
            }
        }
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            ivSave.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                    val songLocations = arrayListOf<String>()
                    mainViewModel.queue.forEach {
                        songLocations.add(it.location)
                    }
                    withContext(Dispatchers.Main) {
                        val bundle = Bundle().apply {
                            putStringArrayList(NavigationConstant.AddToPlaylistSongs, songLocations)
                        }
                        findNavController().navigate(R.id.action_to_addToPlaylistFragment, bundle)
                    }
                }
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.currentSong.collect {
                    updateCurrentSongInfo(it)
                }
            }
        }
    }

    private fun updateCurrentSongInfo(currentSong: Song?) {
        binding.apply {
            currentSong?.let {
                songTitle.text = it.title
                songArtist.text = it.artist
                songDuration.text = it.durationFormatted

                songArt.load(Uri.parse(it.artUri)) {
                    placeholder(R.drawable.logo)
                    error(R.drawable.logo)
                    transformations(RoundedCornersTransformation(10f))
                    crossfade(true)
                }
            }
        }
    }

    private fun songClicked(song: Song, position: Int) {
        mainViewModel.playerHelper.seekTo(position, 0)
    }
}