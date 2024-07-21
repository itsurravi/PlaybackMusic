package com.ravisharma.playbackmusic.new_work.ui.fragments.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ravisharma.playbackmusic.BuildConfig
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.activities.AboutActivity
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.databinding.AlertTimerBinding
import com.ravisharma.playbackmusic.databinding.FragmentHomeBinding
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.services.TimerWorker
import com.ravisharma.playbackmusic.new_work.viewmodel.MainViewModel
import com.ravisharma.playbackmusic.new_work.viewmodel.MusicScanViewModel
import com.ravisharma.playbackmusic.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels()
    private val musicViewModel: MusicScanViewModel by activityViewModels()

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
            ivSearch.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
            }
            ivEqualizer.setOnClickListener {
                // TODO navigate to equalizer screen
            }
            ivMoreOptions.setOnClickListener {
                showMoreOptionsPopup(it)
            }
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

    private fun showMoreOptionsPopup(it: View) {
        PopupMenu(requireContext(), it).apply {
            menuInflater.inflate(R.menu.main, menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.timer -> {
                        showTimer()
                        true
                    }

                    R.id.rescan -> {
                        musicViewModel.scanForMusic()
                        true
                    }

                    R.id.share -> {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player")
                            var shareMessage =
                                "\nDownload the light weight Playback Music Player app\n"
                            shareMessage =
                                "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                            startActivity(Intent.createChooser(shareIntent, "choose one"))
                        } catch (e: Exception) {

                        }
                        true
                    }

                    R.id.rateUs -> {
                        try {
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                            )
                        } catch (e: ActivityNotFoundException) {
                            requireContext().showToast(getString(R.string.unableToFindMarketApp))
                        }
                        true
                    }

                    R.id.suggestion -> {
                        val email = Intent(Intent.ACTION_SEND)
                        email.putExtra(Intent.EXTRA_EMAIL, arrayOf("sharmaravi.23960@gmail.com"))
                        email.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player Suggestion")
                        email.putExtra(Intent.EXTRA_TEXT, "")
                        email.type = "message/rfc822"
                        startActivity(Intent.createChooser(email, "Choose an Email client :"))
                        true
                    }

                    R.id.about -> {
                        startActivity(Intent(requireContext(), AboutActivity::class.java))
                        true
                    }

                    else -> false
                }
            }
        }.also {
            it.show()
        }
    }

    private fun initToolbar() {
        binding.apply {
//            toolbar.title = getString(R.string.app_name)
//            toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.titleColor))

        }
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

    private var alert_seek_max = 5
    private var alert_seek_step = 1
    private var alert_current_value = 0
    private lateinit var seekValue: Array<String>
    private var TIMER = false

    private fun showTimer() {
        val timerArray: Array<String> = resources.getStringArray(R.array.timer)
        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
        val dialogBinding: AlertTimerBinding = AlertTimerBinding.inflate(layoutInflater)

        dialog.setView(dialogBinding.root)
        dialogBinding.alertSeekBar.max = alert_seek_max / alert_seek_step
        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (!TIMER) {
            alertDialog.setCancelable(true)
            dialogBinding.timerBlocker.visibility = View.VISIBLE
            seekValue = timerArray
            switchTimerAlertView(true, dialogBinding.txtTimer, dialogBinding.txtTracks)
            dialogBinding.alertSeekBar.progress = 0
            alert_current_value = 0
        } else {
            alertDialog.setCancelable(false)
            dialogBinding.timerBlocker.visibility = View.GONE
            seekValue = timerArray
            switchTimerAlertView(
                true,
                dialogBinding.txtTimer,
                dialogBinding.txtTracks
            )
            dialogBinding.alertSeekBar.progress = alert_current_value
            dialogBinding.txtOnOff.text = "On"
        }
        dialogBinding.timerSwitch.isChecked = TIMER
        dialogBinding.txtSeekValue.text = seekValue[alert_current_value].toString()
        alertDialog.show()
        dialogBinding.alertSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                alert_current_value = progress * alert_seek_step
                dialogBinding.txtSeekValue.text = seekValue[progress].toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        dialogBinding.txtTimer.setOnClickListener {
            seekValue = timerArray
            switchTimerAlertView(true, dialogBinding.txtTimer, dialogBinding.txtTracks)
            dialogBinding.alertSeekBar.progress = 0
            dialogBinding.txtSeekValue.text = seekValue[0].toString()
        }
        dialogBinding.timerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                TIMER = false
                dialogBinding.txtOnOff.text = "Off"
                dialogBinding.timerBlocker.visibility = View.VISIBLE
                alertDialog.setCancelable(true)
                /*if (am != null && pi != null) {
                    am!!.cancel(pi!!)
                    am = null
                    pi = null
                    requireContext().showToast(getString(R.string.timeOff))
                }*/
                TimerWorker.cancelSleepTimer(requireContext())
            } else {
                TIMER = true
                dialogBinding.txtOnOff.text = "On"
                dialogBinding.timerBlocker.visibility = View.GONE
                alertDialog.setCancelable(false)
            }
        }
        dialogBinding.txtSave.setOnClickListener {
            when (alert_current_value) {
                0 -> {
                    setTimer(15 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins15))
                }

                1 -> {
                    setTimer(30 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins30))
                }

                2 -> {
                    setTimer(45 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins45))
                }

                3 -> {
                    setTimer(60 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins60))
                }

                4 -> {
                    setTimer(90 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins90))
                }

                5 -> {
                    setTimer(120 * 60 * 1000)
                    requireContext().showToast(getString(R.string.mins120))
                }
            }
            alertDialog.dismiss()
        }
    }

    private fun setTimer(time: Int) {
        /*val i = Intent(requireContext(), Timer::class.java)
        pi = PendingIntent.getBroadcast(requireContext(), 1234, i, PendingIntent.FLAG_IMMUTABLE)
        am = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
        am!![AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time] = pi*/

        try {
            TimerWorker.scheduleSleepTimer(time.toLong(), requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun switchTimerAlertView(timerSelected: Boolean, timer: TextView, tracks: TextView) {
        if (timerSelected) {
            timer.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.popupItemBackground
                )
            )
            tracks.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            timer.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.timer_alert_tab_selected_left
            )
            tracks.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.timer_alert_tab_unselected_right
            )
        } else {
            tracks.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.popupItemBackground
                )
            )
            timer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tracks.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.timer_alert_tab_selected_right
            )
            timer.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.timer_alert_tab_unselected_left
            )
        }
    }
}