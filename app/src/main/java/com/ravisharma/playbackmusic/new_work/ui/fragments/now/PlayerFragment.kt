package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.utils.toMS
import com.ravisharma.playbackmusic.databinding.FragmentPlayerBinding
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.ui.extensions.showSongInfo
import com.ravisharma.playbackmusic.new_work.utils.Constants
import com.ravisharma.playbackmusic.new_work.utils.DynamicThemeManager
import com.ravisharma.playbackmusic.new_work.utils.NavigationConstant
import com.ravisharma.playbackmusic.new_work.utils.changeNavigationBarMargin
import com.ravisharma.playbackmusic.new_work.utils.changeStatusBarMargin
import com.ravisharma.playbackmusic.new_work.utils.linearGradientBackground
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()

    private var currentSong: Song? = null

    private var currentProgressValue: Long = 0L

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var themeManager: DynamicThemeManager

    private var adView: AdView? = null
    private var adUnitId: String? = null

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

    private val pendingPreviousIntent by lazy {
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.PREVIOUS_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_PREVIOUS
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private val pendingNextIntent by lazy {
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.NEXT_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_NEXT
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayerBinding.bind(view)

        setupFragment()
    }

    private fun setupFragment() {
        adView = AdView(requireContext())
        initViews()
        initObservers()

        adUnitId = getString(R.string.nowPlayingActId)
        loadBanner()
    }

    private fun loadBanner() {
        adUnitId?.let { unitId ->
            val adRequest = AdRequest.Builder().build()
            val adSize = AdSize.BANNER
            adView!!.adUnitId = unitId
            adView!!.setAdSize(adSize)

            binding.bannerContainerPlayer.addView(adView)

            adView!!.loadAd(adRequest)
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                mainViewModel.currentSong.collect {
                    setCurrentPlayingSong(it)
                }
            }
            launch {
                mainViewModel.currentSongPlaying.collectLatest {
                    togglePlaying(it)
                }
            }
            launch {
                mainViewModel.currentAudioProgress().collect { progress ->
                    updateSeekbarValue(progress)
                }
            }
            launch {
                mainViewModel.repeatMode.collect {
                    updateRepeatMode(it)
                }
            }
            launch {
                mainViewModel.shuffle.collect {
                    updateShuffleMode(it)
                }
            }
        }
    }

    private fun updateShuffleMode(isShuffled: Boolean) {
        Log.i("updateShuffleMode", "$isShuffled")
    }

    private fun togglePlaying(playing: Boolean?) {
        playing?.let {
            binding.apply {
                btnPlayPause.setImageResource(
                    if (it) R.drawable.uamp_ic_pause_white_48dp else R.drawable.uamp_ic_play_arrow_white_48dp
                )
            }
        }
    }

    private fun setCurrentPlayingSong(song: Song?) {
        currentSong = song
        song?.let {
            binding.apply {
                txtSongName.text = it.title
                txtSongArtist.text = it.artist
                totalDuration.text = it.durationMillis.toMS()

                cardImage.load(Uri.parse(it.artUri)) {
                    error(R.drawable.logo)
                    transformations(RoundedCornersTransformation(20f))
                    crossfade(300)
                }

                setDynamicBackground(it.artUri)

                if (it.favourite) {
                    imgFav.setImageResource(R.drawable.ic_favorite_24)
                    imgFav.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.fav_on
                        )
                    )
                } else {
                    imgFav.setImageResource(R.drawable.ic_favorite_not_24)
                    imgFav.imageTintList = ColorStateList.valueOf(Color.WHITE)
                }

                seekBar.max = it.durationMillis.toInt()
                seekBar.progress = exoPlayer.currentPosition.toInt()
            }
        }
    }

    private fun setDynamicBackground(artUri: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            artUri?.let {
                val color = themeManager.getBackgroundColorForImageFromUrl(it, requireContext())
                color?.let { it1 ->
                    binding.rootView.background =
                        binding.rootView.linearGradientBackground(it1)
                }
            }
        }
    }

    private fun initViews() {
        initSeekbarListener()
        initClickListeners()
        binding.topBar.changeStatusBarMargin()
        binding.playerController.changeNavigationBarMargin()
    }

    /*private fun currentAudioProgress(exoPlayer: ExoPlayer) = flow {
        while (true) {
            emit(
                withContext(Dispatchers.Main) {
                    exoPlayer.currentPosition
                }
            )
            delay(100)
        }
    }.flowOn(Dispatchers.IO)*/

    private fun updateSeekbarValue(value: Long) {
        binding.apply {
            currentProgressValue = value

            seekBar.progress = currentProgressValue.toInt()
            currentPosition.text = currentProgressValue.toMS()
        }
    }

    private fun initSeekbarListener() {
        binding.apply {
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        currentProgressValue = progress.toLong()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    exoPlayer.seekTo(currentProgressValue)
                }
            })
        }
    }

    private fun updateRepeatMode(repeatMode: RepeatMode) {
        binding.apply {
            btnRepeat.setImageResource(repeatMode.iconResource)
            val tintColor = ContextCompat.getColor(requireContext(), repeatMode.tintColor)
            btnRepeat.imageTintList = ColorStateList.valueOf(tintColor)
        }
    }

    private fun initClickListeners() {
        binding.apply {
            btnPrev.setOnClickListener {
                pendingPreviousIntent.send()
            }
            btnNext.setOnClickListener {
                pendingNextIntent.send()
            }
            btnPlayPause.setOnClickListener {
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
            btnRepeat.setOnClickListener {
                mainViewModel.toggleRepeatMode()
            }
            imgPlaylist.setOnClickListener {
                findNavController().navigate(R.id.action_playerFragment_to_currentQueueFragment)
            }
            imgFav.setOnClickListener {
                mainViewModel.changeFavouriteValue()
//                mainViewModel.toggleShuffle()
            }
            ivMoreOptions.setOnClickListener {
                showMoreOptionsPopup(it)
            }
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun showMoreOptionsPopup(view: View) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(R.menu.player_menu, menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.addToPlaylist -> {
                        val bundle = Bundle().apply {
                            putStringArrayList(
                                NavigationConstant.AddToPlaylistSongs,
                                arrayListOf(currentSong?.location)
                            )
                        }
                        findNavController().navigate(R.id.action_to_addToPlaylistFragment, bundle)
                        true
                    }

                    R.id.info -> {
                        currentSong?.let { requireContext().showSongInfo(it) }
                        true
                    }

                    else -> false
                }
            }
        }.also {
            it.show()
        }
    }
}