package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

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
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.databinding.FragmentArtistBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.ArtistsAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.HomeViewModel
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArtistsFragment : Fragment(R.layout.fragment_artist) {

    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObserver()
    }

    private fun initViews() {
        binding.apply {
            artistList.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = ArtistsAdapter(onItemClick = ::onArtistClick)
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allArtists.collectLatest { list ->
                setupData(list)
            }
        }
    }

    private fun setupData(list: List<ArtistWithSongCount>) {
        binding.apply {
            if (list.isNotEmpty()) {
                (artistList.adapter as ArtistsAdapter).submitList(list)
            }
        }
    }

    private fun onArtistClick(artist: ArtistWithSongCount) {
        requireContext().showToast("artist click")
    }

    companion object {
        @JvmStatic
        fun getInstance(): ArtistsFragment {
            return ArtistsFragment()
        }
    }
}