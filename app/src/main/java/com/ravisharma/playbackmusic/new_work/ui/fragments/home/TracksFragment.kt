package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentNameWiseBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.TracksAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.HomeViewModel
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TracksFragment : Fragment(R.layout.fragment_name_wise) {

    private var _binding: FragmentNameWiseBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNameWiseBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObserver()
    }

    private fun initViews() {
        binding.apply {
            songList.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = TracksAdapter(
                    onItemClick = ::songClicked,
                    onItemLongClick = ::songLongClicked
                )
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allSongs.collectLatest { list ->
                setupData(list)
            }
        }
    }

    private fun setupData(list: List<Song>) {
        binding.apply {
            if(list.isNotEmpty()) {
                (songList.adapter as TracksAdapter).submitList(list)
            }
        }
    }

    private fun songClicked(song: Song) {
        requireContext().showToast("song click")
    }

    private fun songLongClicked(song: Song) {
        requireContext().showToast("song long click")
    }
}