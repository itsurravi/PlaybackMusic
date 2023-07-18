package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentHomeBinding
import com.ravisharma.playbackmusic.new_work.ui.adapters.HomePageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initToolbar()
        initPager()
        initObservers()
    }

    private fun initToolbar() {
        binding.apply {
            toolbar.title = getString(R.string.app_name)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.titleColor))
        }
    }

    private fun initPager() {
        binding.apply {
            val pagerAdapter = HomePageAdapter(requireActivity())
            viewPager.apply {
                offscreenPageLimit = 4
                adapter = pagerAdapter
            }

            TabLayoutMediator(tabs, viewPager) { tab: TabLayout.Tab, position: Int ->
                when (position) {
                    0 -> tab.text = getString(R.string.playlist)
                    1 -> tab.text = getString(R.string.Tracks)
                    2 -> tab.text = getString(R.string.Albums)
                    3 -> tab.text = getString(R.string.Artists)
                }
            }.attach()

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    fabShuffle.isVisible = position == 1
                }
            })
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                homeViewModel.currentSong.collect { currentSong ->
                    updateBottomPanel(currentSong)
                }
            }
            launch {
                homeViewModel.currentSongPlaying.collect { playing ->
                    updateBottomPanel(playing)
                }
            }
        }
    }

    private fun updateBottomPanel(currentSong: Song?) {
        binding.apply {
            currentSong?.let {
                if (!bottomPanel.isVisible) {
                    bottomPanel.isVisible = true
                }
                slideImage.load(Uri.parse(it.artUri)) {
                    error(R.drawable.logo)
                    transformations(RoundedCornersTransformation(10f))
                    crossfade(true)
                }
                txtSongName.text = it.title
                txtSongArtist.text = it.artist
            }
        }
    }

    private fun updateBottomPanel(playing: Boolean?) {
        playing?.let {
            binding.apply {
                btnPlayPauseSlide.setImageResource(
                    if (it) R.drawable.uamp_ic_pause_white_48dp else R.drawable.uamp_ic_play_arrow_white_48dp
                )
            }
        }
    }
}