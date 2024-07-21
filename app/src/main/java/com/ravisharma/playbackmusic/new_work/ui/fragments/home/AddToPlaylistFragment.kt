package com.ravisharma.playbackmusic.new_work.ui.fragments.home

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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.databinding.ActivityAddToPlaylistBinding
import com.ravisharma.playbackmusic.new_work.NavigationConstant
import com.ravisharma.playbackmusic.new_work.ui.adapters.PlaylistsAdapter
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddToPlaylistFragment : Fragment(R.layout.activity_add_to_playlist) {

    private var _binding: ActivityAddToPlaylistBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var songLocation = mutableListOf<String>()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songLocation = it.getStringArrayList(NavigationConstant.AddToPlaylistSongs) ?: arrayListOf()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ActivityAddToPlaylistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())

        initView()
        initObservers()

        adUnitId = getString(R.string.addToPlaylistFragId)
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

    private fun initView() {
        binding.apply {
            imgBack.setOnClickListener {
                findNavController().popBackStack()
            }
            btnAddNewPlaylist.setOnClickListener {
                createPlaylistDialog()
            }
            playlistRecycler.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = PlaylistsAdapter(
                    onClick = ::onPlaylistClick,
                )
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.playlistsWithSongCount.collect {
                    updateUi(it)
                }
            }
        }
    }

    private fun updateUi(list: List<PlaylistWithSongCount>) {
        (binding.playlistRecycler.adapter as PlaylistsAdapter).submitList(list)
    }

    private fun onPlaylistClick(playlist: PlaylistWithSongCount) {
        if (songLocation.size == 1) {
            mainViewModel.addSongToPlaylist(playlist.playlistId, songLocation[0])
        } else {
            mainViewModel.addSongsToPlaylist(playlist.playlistId, songLocation)
        }

        findNavController().popBackStack()
    }

    private fun createPlaylistDialog() {
        val listener = AlertClickListener { newPlaylistName ->
            mainViewModel.createPlaylist(newPlaylistName)
        }
        val alert = PlaylistAlert(context, listener)
        alert.showCreateListAlert()
    }
}