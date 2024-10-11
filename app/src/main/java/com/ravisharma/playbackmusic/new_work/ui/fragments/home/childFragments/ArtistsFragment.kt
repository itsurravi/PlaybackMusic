package com.ravisharma.playbackmusic.new_work.ui.fragments.home.childFragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.ArtistWithSongCount
import com.ravisharma.playbackmusic.databinding.FragmentArtistBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.ArtistsAdapter
import com.ravisharma.playbackmusic.new_work.ui.fragments.category.CollectionType
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArtistsFragment : Fragment(R.layout.fragment_artist) {

    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var adView: AdView? = null
    private var adUnitId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())

        initViews()
        initObserver()

        adUnitId = getString(R.string.artistFragId)
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
            artistList.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(MaterialDividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                    dividerColor = ContextCompat.getColor(requireContext(), R.color.divider)
                })
                adapter = ArtistsAdapter(onItemClick = ::onArtistClick)
            }
        }
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            mainViewModel.allArtists.collectLatest { list ->
                list?.let {
                    setupData(it)
                }
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
        val collectionType = CollectionType(CollectionType.ArtistType, artist.name)
        val bundle = Bundle().apply {
            putParcelable(CollectionType.Category, collectionType)
        }
        requireActivity().findNavController(R.id.nav_container)
            .navigate(R.id.action_homeFragment_to_categoryListingFragment, bundle)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ArtistsFragment {
            return ArtistsFragment()
        }
    }
}