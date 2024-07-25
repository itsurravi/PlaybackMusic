package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Album
import com.ravisharma.playbackmusic.databinding.FragmentAlbumsBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.AlbumsAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.category.CollectionType
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumsFragment : Fragment(R.layout.fragment_albums) {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumsBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())

        initViews()
        initObserver()

        adUnitId = getString(R.string.albumFragId)
        loadBanner()
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerAd.addView(adView)

//            adView!!.loadAd(adRequest)
        }
    }

    private fun initViews() {
        binding.apply {
            albumList.apply {
                layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                adapter = AlbumsAdapter(onItemClick = ::onAlbumClick)
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.allAlbums.collectLatest { list ->
                list?.let {
                    setupData(it)
                }
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
        val collectionType = CollectionType(CollectionType.AlbumType, album.name)
        val bundle = Bundle().apply {
            putParcelable(CollectionType.Category, collectionType)
        }
        requireActivity().findNavController(R.id.nav_container).navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
    }

    companion object {
        @JvmStatic
        fun getInstance(): AlbumsFragment {
            return AlbumsFragment()
        }
    }
}