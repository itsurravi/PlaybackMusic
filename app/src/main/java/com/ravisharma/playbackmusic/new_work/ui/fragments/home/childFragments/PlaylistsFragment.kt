package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.databinding.FragmentPlaylistBinding
import com.ravisharma.playbackmusic.nativetemplates.NativeTemplateStyle
import com.ravisharma.playbackmusic.new_work.ui.adapters.PlaylistsAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.category.CollectionType
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlaylistsFragment : Fragment(R.layout.fragment_playlist) {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())
        initViews()
        initObservers()

        adUnitId = getString(R.string.playlistFragId)
        loadBanner()
    }

    private fun setupAdLoader() {
//        val builder = AdLoader.Builder(requireContext(), getString(R.string.nativeAdvanceId))
        val builder = AdLoader.Builder(requireContext(), getString(R.string.testNativeAdvanceId))
        builder.forNativeAd { nativeAd ->
            val styles = NativeTemplateStyle.Builder().build()
            binding.myTemplate.setStyles(styles)
            binding.myTemplate.setNativeAd(nativeAd)
            binding.myTemplate.visibility = View.VISIBLE
        }
/*
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val adOptions = NativeAdOptions.Builder().setVideoOptions(videoOptions).build()
        builder.withNativeAdOptions(adOptions)*/

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val error = "domain: ${loadAdError.domain}, " +
                        "code: ${loadAdError.code}, " +
                        "message: ${loadAdError.message}"
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun loadBanner() {
        setupAdLoader()

        /*adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerAd.addView(adView)

            adView!!.loadAd(adRequest)
        }*/
    }

    private fun initViews() {
        binding.apply {
            btnAddNewPlaylist.setOnClickListener {
                createPlaylistDialog()
            }
            cardMostPlayed.setOnClickListener {
                val collectionType =
                    CollectionType(CollectionType.MostPlayedType, "")
                val bundle = Bundle().apply {
                    putParcelable(CollectionType.Category, collectionType)
                }
                requireActivity().findNavController(R.id.nav_container).navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
            }
            cardRecentAdded.setOnClickListener {
                val collectionType =
                    CollectionType(CollectionType.RecentAddedType, "")
                val bundle = Bundle().apply {
                    putParcelable(CollectionType.Category, collectionType)
                }
                requireActivity().findNavController(R.id.nav_container).navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
            }
            cardFavorites.setOnClickListener {
                val collectionType =
                    CollectionType(CollectionType.FavouritesType, "")
                val bundle = Bundle().apply {
                    putParcelable(CollectionType.Category, collectionType)
                }
                requireActivity().findNavController(R.id.nav_container).navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
            }

            playlistRecycler.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = PlaylistsAdapter(
                    onClick = ::onPlaylistClick,
                    onLongClick = ::onPlaylistLongClick
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
        val collectionType =
            CollectionType(CollectionType.PlaylistType, playlist.playlistId.toString())
        val bundle = Bundle().apply {
            putParcelable(CollectionType.Category, collectionType)
        }
        requireActivity().findNavController(R.id.nav_container).navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
    }

    private fun onPlaylistLongClick(playlistWithSongCount: PlaylistWithSongCount) {
        val items = resources.getStringArray(R.array.longPressItemsPlaylist)
        val ad = ArrayAdapter(requireContext(), R.layout.adapter_alert_list, items)
        val v = LayoutInflater.from(context).inflate(R.layout.alert_playlist, null)
        val lv = v.findViewById<ListView>(R.id.list)
        val tv = v.findViewById<TextView>(R.id.title)
        tv.text = playlistWithSongCount.playlistName
        lv.adapter = ad
        val dialog = AlertDialog.Builder(
            requireContext()
        )
        dialog.setView(v)
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.DialogAnimation_2
        alertDialog.show()
        lv.onItemClickListener = AdapterView.OnItemClickListener { parent, view, i, id ->
            when (i) {
                0 -> playPlaylistSongs(playlistWithSongCount)
                1 -> updatePlaylistDialog(playlistWithSongCount)
                2 -> mainViewModel.deletePlaylist(playlistWithSongCount)
            }
            alertDialog.dismiss()
        }
    }

    private fun playPlaylistSongs(playlistWithSongCount: PlaylistWithSongCount) {
        mainViewModel.playPlaylistSongs(playlistWithSongCount)
    }

    private fun createPlaylistDialog() {
        val listener = AlertClickListener { newPlaylistName ->
            mainViewModel.onPlaylistCreate(newPlaylistName)
        }
        val alert = PlaylistAlert(context, listener)
        alert.showCreateListAlert()
    }

    private fun updatePlaylistDialog(oldPlaylistName: PlaylistWithSongCount) {
        val listener = AlertClickListener { newPlaylistName ->
            mainViewModel.updatePlaylistName(oldPlaylistName.copy(
                playlistName = newPlaylistName
            ))
        }
        val alert = PlaylistAlert(context, listener)
        alert.showUpdateListAlert(oldPlaylistName.playlistName)
    }

    companion object {
        @JvmStatic
        fun getInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}