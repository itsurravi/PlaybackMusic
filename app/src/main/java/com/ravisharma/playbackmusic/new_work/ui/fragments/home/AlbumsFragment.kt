package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.databinding.FragmentAlbumsBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.AlbumsAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumsFragment : Fragment(R.layout.fragment_albums) {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumsBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObserver()
    }

    private fun initViews() {
        binding.apply {
            albumList.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = AlbumsAdapter(onItemClick = ::onAlbumClick)
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allAlbums.collectLatest { list ->
                setupData(list)
            }
        }
    }

    private fun setupData(list: List<Album>) {
        binding.apply {
            if (list.isNotEmpty()) {
                (albumList.adapter as AlbumsAdapter).submitList(list)
            }
        }
    }

    private fun onAlbumClick(album: Album) {
        // TODO
    }
}