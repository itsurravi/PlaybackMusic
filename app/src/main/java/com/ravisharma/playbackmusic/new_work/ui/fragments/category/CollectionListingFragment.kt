package com.ravisharma.playbackmusic.new_work.ui.fragments.category

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentCollectionListingBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.TracksAdapter
import com.ravisharma.playbackmusic.new_work.ui.extensions.LongItemClick
import com.ravisharma.playbackmusic.new_work.ui.extensions.onPlaylistSongLongPress
import com.ravisharma.playbackmusic.new_work.ui.extensions.onSongLongPress
import com.ravisharma.playbackmusic.new_work.ui.extensions.shareSong
import com.ravisharma.playbackmusic.new_work.ui.extensions.showSongInfo
import com.ravisharma.playbackmusic.new_work.ui.extensions.showToast
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.utils.changeNavigationBarPadding
import com.ravisharma.playbackmusic.new_work.utils.changeStatusBarMargin
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.ArrayList

@AndroidEntryPoint
class CollectionListingFragment : Fragment(R.layout.fragment_collection_listing) {

    private var _binding: FragmentCollectionListingBinding? = null
    private val binding get() = _binding!!

    private val collectionViewModel: CollectionViewModel by viewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    private var reorderListAllowed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val collectionType: CollectionType? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(CollectionType.Category, CollectionType::class.java)
                } else {
                    it.getParcelable(CollectionType.Category)
                }

            collectionType?.let {
                collectionViewModel.loadCollection(collectionType)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCollectionListingBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())

        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            firstLayout.isVisible = true

            songList.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    MaterialDividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    ).apply {
                        dividerColor = ContextCompat.getColor(requireContext(), R.color.divider)
                    })
                adapter = TracksAdapter(
                    onItemClick = ::songClicked,
                    onItemLongClick = ::songLongClicked
                )
            }

            cvPlay.setOnClickListener {
                collectionViewModel.setQueue(getCurrentList(), 0)
            }
            imageBack1.setOnClickListener {
                findNavController().popBackStack()
            }
            imgOptions.setOnClickListener {
                showMoreOptionsMenu(it)
            }
            toolbar.changeStatusBarMargin()
            songList.changeNavigationBarPadding()
        }
    }

    private fun showMoreOptionsMenu(it: View) {
        PopupMenu(requireContext(), it).apply {
            menuInflater.inflate(R.menu.collection_menu, menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.addToQueue -> {
                        collectionViewModel.addToQueue(getCurrentList())
                        true
                    }

                    R.id.addToPlaylist -> {
                        val bundle = Bundle().apply {
                            putStringArrayList(
                                NavigationConstant.AddToPlaylistSongs,
                                getCurrentList().map {
                                    it.location
                                } as ArrayList<String>
                            )
                        }
                        findNavController().navigate(R.id.action_to_addToPlaylistFragment, bundle)
                        true
                    }

                    else -> false
                }
            }
        }.also {
            it.show()
        }
    }

    private fun getCurrentList(): List<Song> {
        return (binding.songList.adapter as TracksAdapter).getCurrentList()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                collectionViewModel.collectionUi.collect {
                    it?.let { it1 -> updateUi(it1) }
                }
            }
            launch {
                collectionViewModel.collectionType.collect {
                    when (it?.type) {
                        CollectionType.AlbumType -> {
                            adUnitId = getString(R.string.AlbumSongsActId)
                        }

                        CollectionType.ArtistType -> {
                            adUnitId = getString(R.string.artistSongsActId)
                        }

                        CollectionType.PlaylistType -> {
                            reorderListAllowed = true
                            adUnitId = getString(R.string.playlistActId)
                        }

                        CollectionType.FavouritesType -> {
                            reorderListAllowed = true
                            adUnitId = getString(R.string.SingleSongActId)
                        }

                        CollectionType.RecentAddedType -> {
                            adUnitId = getString(R.string.recentSongsActId)
                        }

                        else -> {
                            adUnitId = getString(R.string.artistFragId)
                        }
                    }
                    loadBanner()
                }
            }
            launch {
                collectionViewModel.message.collect {
                    if (it.isNotEmpty()) {
                        requireContext().showToast(it)
                    }
                }
            }
        }
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerContainerRecentActivity.addView(adView)

            adView!!.loadAd(adRequest)
        }
    }

    private fun updateUi(data: CollectionUi) {
        binding.apply {
            txtPlaylistName1.text = data.topBarTitle
            val songCount = data.songs.size
            noOfSongs.text =
                resources.getQuantityString(R.plurals.numberOfSongs, songCount, songCount)

            albumArt.load(data.topBarBackgroundImageUri) {
                placeholder(R.drawable.logo)
                error(R.drawable.logo)
                crossfade(true)
            }

            if (data.songs.isEmpty()) {
                noDataFound.noDataLayout.isVisible = true
                cvPlay.isVisible = false
                imgOptions.isVisible = false
            } else {
                noDataFound.noDataLayout.isVisible = false
                cvPlay.isVisible = true
                imgOptions.isVisible = true
                (songList.adapter as TracksAdapter).submitList(data.songs)
            }
        }
    }

    private fun songClicked(song: Song, position: Int) {
        collectionViewModel.setQueue(getCurrentList(), position)
    }

    private fun songLongClicked(song: Song, position: Int) {
        if (reorderListAllowed) {
            requireActivity().onPlaylistSongLongPress(song) { longItemClick ->
                longClickItem(longItemClick, song, position)
            }
        } else {
            requireActivity().onSongLongPress(song) { longItemClick ->
                longClickItem(longItemClick, song, position)
            }
        }
    }

    private fun longClickItem(longItemClick: LongItemClick, song: Song, position: Int) {
        when (longItemClick) {
            LongItemClick.Play -> {
                songClicked(song, position)
            }

            LongItemClick.SinglePlay -> {
                collectionViewModel.setQueue(listOf(song), 0)
            }

            LongItemClick.AddToQueue -> {
                collectionViewModel.addToQueue(song)
            }

            LongItemClick.RemoveFromList -> {
                collectionViewModel.removeFromPlaylist(song)
            }

            LongItemClick.Share -> {
                requireContext().shareSong(song.location)
            }

            LongItemClick.Details -> {
                requireActivity().showSongInfo(song)
            }

            LongItemClick.AddToPlaylist -> {
                val bundle = Bundle().apply {
                    putStringArrayList(
                        NavigationConstant.AddToPlaylistSongs,
                        arrayListOf(song.location)
                    )
                }
                findNavController().navigate(R.id.action_to_addToPlaylistFragment, bundle)
            }

            else -> Unit
        }
    }
}