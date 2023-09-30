package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.FragmentHomeBinding
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var isLastPlayedListSet = false

    private val pendingPausePlayIntent by lazy {
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.PAUSE_PLAY_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_PAUSE_PLAY
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        initViews()
        initObservers()
        initDefaultData()
    }

    private fun initDefaultData() {
        if (!isLastPlayedListSet) {
            isLastPlayedListSet = true
            mainViewModel.setLastPlayedList()
        }
    }

    private fun initViews() {
        initToolbar()
        initClickListeners()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.apply {
            val navController =
                (childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

            bottomNavigationView.setupWithNavController(navController = navController)
        }
    }

    private fun initClickListeners() {
        binding.apply {
            bottomPanel.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
            }
            btnPlayPauseSlide.setOnClickListener {
                try {
                    if (mainViewModel.isServiceInitialized()) {
                        pendingPausePlayIntent.send()
                    } else {
                        mainViewModel.startPlaying()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initToolbar() {
        binding.apply {
            toolbar.title = getString(R.string.app_name)
            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.titleColor))
        }
    }

    private fun initPager() {
        /*binding.apply {
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
        }*/
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.currentSong.collect { currentSong ->
                    updateBottomPanel(currentSong)
                }
            }
            launch {
                mainViewModel.currentSongPlaying.collect { playing ->
                    updateBottomPanel(playing)
                }
            }
            launch {
                mainViewModel.currentAudioProgress().collect { progress ->
                    updateSeekbarValue(progress)
                }
            }
        }
    }

    private fun updateSeekbarValue(progress: Long) {
        binding.apply {
            progressSong.progress = progress.toInt()
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
                progressSong.max = it.durationMillis.toInt()
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