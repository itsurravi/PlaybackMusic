package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.utils.toMS
import com.ravisharma.playbackmusic.databinding.FragmentPlayerBinding
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.ui.fragments.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var currentSong: Song? = null

    private var currentProgressValue: Long = 0L

    @Inject
    lateinit var exoPlayer: ExoPlayer

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
        initViews()
        initObservers()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                homeViewModel.currentSong.collect {
                    setCurrentPlayingSong(it)
                }
            }
            launch {
                homeViewModel.currentSongPlaying.collectLatest {
                    togglePlaying(it)
                }
            }
            launch {
                currentAudioProgress(exoPlayer).collect { progress ->
                    updateSeekbarValue(progress)
                }
            }
            launch {
                homeViewModel.repeatMode.collect {
                    updateRepeatMode(it)
                }
            }
            launch {
                homeViewModel.shuffleMode.collect {
                    updateShuffleMode(it)
                }
            }
        }
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
                    crossfade(true)
                }

                imgFav.setImageResource(
                    if (it.favourite) {
                        R.drawable.ic_fav
                    } else {
                        R.drawable.ic_fav_not
                    }
                )

                seekBar.max = it.durationMillis.toInt()
                seekBar.progress = exoPlayer.currentPosition.toInt()
            }
        }
    }

    private fun initViews() {
        initSeekbarListener()
        initClickListeners()
    }

    private fun currentAudioProgress(exoPlayer: ExoPlayer) = flow {
        while (true) {
            emit(
                withContext(Dispatchers.Main) {
                    exoPlayer.currentPosition
                }
            )
            delay(100)
        }
    }.flowOn(Dispatchers.IO)

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
        }
    }

    private fun updateShuffleMode(shuffle: Boolean) {
        binding.apply {
            if (shuffle) {
                btnShuffle.setImageResource(R.drawable.ic_shuffle)
            } else {
                btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            }
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
                pendingPausePlayIntent.send()
            }
            btnShuffle.setOnClickListener {
                homeViewModel.toggleShuffleMode()
            }
            btnRepeat.setOnClickListener {
                homeViewModel.toggleRepeatMode()
            }
            imgPlaylist.setOnClickListener {
                findNavController().navigate(R.id.action_playerFragment_to_currentQueueFragment)
            }
            imgFav.setOnClickListener {
                homeViewModel.changeFavouriteValue()
            }
            imgEq.setOnClickListener {

            }
            ivInfo.setOnClickListener {

            }
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}