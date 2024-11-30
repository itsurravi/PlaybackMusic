package com.ravisharma.playbackmusic.new_work.ui.fragments.search

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentSearchBinding
import com.ravisharma.playbackmusic.new_work.ui.activity.NewPlayerActivity
import com.ravisharma.playbackmusic.new_work.ui.adapters.TracksAdapter
import com.ravisharma.playbackmusic.new_work.ui.extensions.LongItemClick
import com.ravisharma.playbackmusic.new_work.ui.extensions.onSongLongPress
import com.ravisharma.playbackmusic.new_work.ui.extensions.shareSong
import com.ravisharma.playbackmusic.new_work.ui.extensions.showSongInfo
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.utils.changeNavigationBarPadding
import com.ravisharma.playbackmusic.new_work.utils.changeStatusBarPadding
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())
        initViews()
        initObserver()

        adUnitId = getString(R.string.searchActId)
        loadBanner()

        binding.apply {
            imgBack.setOnClickListener {
                findNavController().popBackStack()
            }
            tvClear.setOnClickListener {
                edSearch.text?.clear()
            }
        }
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerContainerSearch.addView(adView)

            adView!!.loadAd(adRequest)
        }
    }

    private fun initViews() {
        binding.apply {
            songList.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(MaterialDividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                    dividerColor = ContextCompat.getColor(requireContext(), R.color.divider)
                })
                adapter = TracksAdapter(
                    onItemClick = ::songClicked,
                    onItemLongClick = ::songLongClicked
                )
            }
            edSearch.doAfterTextChanged {
                val title = it?.toString()
                title?.let { it1 -> searchViewModel.updateQuery(it1) }
            }
            llSearch.changeStatusBarPadding()
            songList.changeNavigationBarPadding()
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            searchViewModel.searchResult.collectLatest {
                setupData(it)
            }
        }
    }

    private fun setupData(list: List<Song>) {
        binding.apply {
            (songList.adapter as TracksAdapter).submitList(list)
        }
    }

    private fun songClicked(song: Song, position: Int) {
        val currentList = (binding.songList.adapter as TracksAdapter).getCurrentList()
        mainViewModel.setQueue(currentList, position)
        findNavController().popBackStack()
    }

    private fun songLongClicked(song: Song, position: Int) {
        requireActivity().onSongLongPress(song) { longItemClick ->
            when (longItemClick) {
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

                LongItemClick.Play -> songClicked(song, position)
                LongItemClick.SinglePlay -> mainViewModel.setQueue(listOf(song), 0)
                LongItemClick.AddToQueue -> mainViewModel.addToQueue(song)
                LongItemClick.Share -> requireContext().shareSong(song.location)
                LongItemClick.Details -> requireActivity().showSongInfo(song)
                else -> Unit
            }
        }
    }
}