package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.PlaylistWithSongCount
import com.ravisharma.playbackmusic.databinding.FragmentPlaylistBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            btnAddNewPlaylist.setOnClickListener {
                createPlaylistDialog()
            }
//            cardLastPlayed.setOnClickListener {
//                 TODO
//            }
//            cardMostPlayed.setOnClickListener {
//                 TODO
//            }
            cardRecentAdded.setOnClickListener {
                val collectionType =
                    CollectionType(CollectionType.RecentAddedType, "")
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

    private fun createPlaylistDialog() {
        val listener = AlertClickListener { newPlaylistName ->
            mainViewModel.createPlaylist(newPlaylistName)
        }
        val alert = PlaylistAlert(context, listener)
        alert.showCreateListAlert()
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
        // TODO
    }

    companion object {
        @JvmStatic
        fun getInstance(): PlaylistsFragment {
            return PlaylistsFragment()
        }
    }
}