package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentNameWiseBinding
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.ui.adapters.TracksAdapter
import com.ravisharma.playbackmusic.new_work.ui.extensions.LongItemClick
import com.ravisharma.playbackmusic.new_work.ui.extensions.onSongLongPress
import com.ravisharma.playbackmusic.new_work.ui.extensions.shareSong
import com.ravisharma.playbackmusic.new_work.ui.extensions.showSongInfo
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TracksFragment : Fragment() {

    private var _binding: FragmentNameWiseBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) {
            _binding = FragmentNameWiseBinding.inflate(inflater, container, false)
            initViews()
            initObserver()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())
        adUnitId = getString(R.string.SingleSongActId)
        loadBanner()
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerAd.addView(adView)

            adView!!.loadAd(adRequest)
        }
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
            fabShuffle.setOnClickListener {
                val list = (binding.songList.adapter as TracksAdapter).getCurrentList()
                mainViewModel.shufflePlay(list)
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.i("TracksFragment", "initObserver")
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.allSongs.collectLatest { list ->
                    Log.i("TracksFragment", "Collected list")
                    list?.let {
                        setupData(it)
                    }
                }
            }
        }
    }

    private fun setupData(list: List<Song>) {
        binding.apply {
            if (list.isNotEmpty()) {
                (songList.adapter as TracksAdapter).submitList(list)
                fabShuffle.isVisible = true
            } else {
                fabShuffle.isVisible = false
            }
        }
    }

    private fun songClicked(song: Song, position: Int) {
        val currentList = (binding.songList.adapter as TracksAdapter).getCurrentList()
        mainViewModel.setQueue(currentList, position)
    }

    private fun songLongClicked(song: Song, position: Int) {
        requireContext().onSongLongPress(song) { longItemClick ->
            when (longItemClick) {
                LongItemClick.Play -> {
                    songClicked(song, position)
                }

                LongItemClick.SinglePlay -> {
                    mainViewModel.setQueue(listOf(song), 0)
                }

//                LongItemClick.PlayNext -> {
//                    mainViewModel.addNextInQueue(song)
//                }

                LongItemClick.AddToQueue -> {
                    mainViewModel.addToQueue(song)
                }

                LongItemClick.AddToPlaylist -> {
                    val bundle = Bundle().apply {
                        putStringArrayList(
                            NavigationConstant.AddToPlaylistSongs,
                            arrayListOf(song.location)
                        )
                    }
                    requireActivity().findNavController(R.id.nav_container)
                        .navigate(R.id.action_to_addToPlaylistFragment, bundle)
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

    companion object {
        @JvmStatic
        fun getInstance(): TracksFragment {
            return TracksFragment()
        }
    }
}