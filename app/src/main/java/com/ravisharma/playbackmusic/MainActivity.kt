package com.ravisharma.playbackmusic

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.ravisharma.playbackmusic.MusicService.MusicBinder
import com.ravisharma.playbackmusic.activities.AboutActivity
import com.ravisharma.playbackmusic.activities.EqualizerActivity
import com.ravisharma.playbackmusic.activities.SearchActivity
import com.ravisharma.playbackmusic.broadcast.Timer
import com.ravisharma.playbackmusic.database.model.LastPlayed
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository
import com.ravisharma.playbackmusic.databinding.ActivityMainBinding
import com.ravisharma.playbackmusic.databinding.AlertPopupMessageBinding
import com.ravisharma.playbackmusic.databinding.AlertTimerBinding
import com.ravisharma.playbackmusic.equalizer.model.EqualizerModel
import com.ravisharma.playbackmusic.equalizer.model.EqualizerSettings
import com.ravisharma.playbackmusic.equalizer.model.Settings
import com.ravisharma.playbackmusic.fragments.*
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import com.ravisharma.playbackmusic.provider.DataManager
import com.ravisharma.playbackmusic.provider.SongsProvider
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener, NameWise.OnFragmentItemClicked,
    NowPlayingFragment.OnFragmentItemClicked, CategorySongFragment.OnFragmentItemClicked {

    private var TAG: String? = null
    private var CHANNEL_ID: String? = null

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var audioManager: AudioManager

    @Inject
    lateinit var manage: PrefManager

    @Inject
    lateinit var tinydb: TinyDB

    @Inject
    lateinit var lastPlayedRepository: LastPlayedRepository

    @Inject
    lateinit var mostPlayedRepository: MostPlayedRepository

    private var adView: AdView? = null
    private var latestVersion: String? = null
    private var currentVersion: String? = null

    private var alert_seek_max = 5
    private var alert_seek_step = 1
    private var alert_current_value = 0
    private var trackCount = 0

    private lateinit var seekValue: Array<String>

    var timerSelectedValue = true

    private lateinit var lastSongId: String
    private var playingDuration: String? = null
    private var lastShuffle = false
    private var lastRepeat = false
    private var lastRepeatOne = false

    var musicSrv: MusicService? = null

    var started = false

    private var TIMER = false
    private var doubleBackToExitPressedOnce = false

    var songList: ArrayList<Song> = ArrayList()
    var normalList: ArrayList<Song> = ArrayList()

    private var songPosn = 0
    private var playIntent: Intent? = null
    private var pi: PendingIntent? = null

    private var am: AlarmManager? = null

    @JvmField
    var mEqualizer: Equalizer? = null

    @JvmField
    var bassBoost: BassBoost? = null

    @JvmField
    var presetReverb: PresetReverb? = null

    @JvmField
    var virtualizer: Virtualizer? = null

    private var playingSong: Song? = null

    @JvmField
    var mediaSession: MediaSession? = null

    @JvmField
    var sessionId = 0

    @JvmField
    var played = false

    var maxVolume = 0
    var curVolume = 0

    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNotificationChannel()

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        lifecycleScope.launchWhenStarted {
            delay(400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermission()
            } else {
                runTask()
            }
        }

        instance = this
        TAG = getString(R.string.app_name)

        binding.toolbar.title = getString(R.string.app_name)
        binding.toolbar.setTitleTextColor(resources.getColor(R.color.titleColor))

        setSupportActionBar(binding.toolbar)
    }

    private fun setNotificationChannel() {
        CHANNEL_ID = getString(R.string.music_Service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val imp = NotificationManager.IMPORTANCE_DEFAULT
            val c = NotificationChannel(CHANNEL_ID, getString(R.string.PlaybackMusicService), imp)
            c.setSound(null, null)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(c)
        }
    }

    /*
     *  Permissions Checking
     * */
    private fun checkPermission() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WAKE_LOCK
                    ) == PackageManager.PERMISSION_GRANTED &&
                    (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS
                    ) == PackageManager.PERMISSION_GRANTED))
        ) {
            runTask()
        } else {
            showPermissionReasonDialog()
        }
    }

    private fun showPermissionReasonDialog() {
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val dialogBinding: AlertPopupMessageBinding =
            AlertPopupMessageBinding.inflate(layoutInflater)
        dialog.setView(dialogBinding.root)

        val alertDialog = dialog.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(false)

        dialogBinding.txtTitle.text = getString(R.string.permissionHead)
        dialogBinding.txtMessage.text = getString(R.string.permissionReason)
        dialogBinding.txtSave.text = getString(R.string.permissionAgree)

        dialogBinding.txtSave.setOnClickListener {
            alertDialog.dismiss()
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
                ), 1
            )
        }
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runTask()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    finish()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.permissionAlert))
                        .setPositiveButton(
                            getString(R.string.Grant)
                        ) { dialog, id ->
                            finish()
                            val intent = Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts(
                                    getString(R.string.packageName),
                                    packageName,
                                    null
                                )
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        .setNegativeButton(
                            getString(R.string.dont)
                        ) { dialog, id -> finish() }
                    builder.setCancelable(false)
                    builder.create().show()
                }
            }
        }
    }

    private fun runTask() {
        clearPrefDataOnAppUpdate()
        val provider = SongsProvider()
        provider.fetchAllData(contentResolver).observe(this@MainActivity) { aBoolean ->
            if (aBoolean) {
                checkInPlaylists()
                Handler(Looper.getMainLooper()).postDelayed({ setUpMainScreen() }, 1000)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
//            dataManager.scanForMusic(contentResolver)
        }
    }

    @Inject
    lateinit var dataManager: DataManager

    private fun clearPrefDataOnAppUpdate() {
        val version = manage.appVersion
        val buildVersion = BuildConfig.VERSION_CODE
        if (version == buildVersion) {
            return
        }
        manage.clearAllData()
        manage.storeAppVersion(buildVersion)
    }

    private fun setUpMainScreen() {
        binding.splashScreen.splashLayout.visibility = View.GONE
        binding.slidingLayout.visibility = View.VISIBLE

        setUpView()
        registerMediaChangeObserver()
    }

    private fun setUpView() {
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE)
        }
        musicSrv = MusicService()
        binding.playingPanel.playerController.alpha = 0f

        val sectionsPagerAdapter = SectionsPagerAdapter(this)

        binding.viewPager.apply {
            offscreenPageLimit = 4
            adapter = sectionsPagerAdapter
        }
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(
            tabLayout,
            binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = getString(R.string.playlist)
                1 -> tab.text = getString(R.string.Tracks)
                2 -> tab.text = getString(R.string.Albums)
                3 -> tab.text = getString(R.string.Artists)
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                binding.fabShuffle.isVisible = position != 0
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        viewModel.getPlayingList().observe(this) { songs ->
            songList = songs
            if (songList.size > 0) {
                Log.d("Playing", "List Changed " + songList.size)
                musicSrv!!.setList(songList)
                if (playingDuration != null && (playingDuration!!.isNotEmpty() || playingDuration!!.isNotBlank())) {
                    musicSrv!!.setPlayingPosition(playingDuration)
                }
                viewModel.saveTinyDbSongs(getString(R.string.NormalSongs), normalList)
                viewModel.saveTinyDbSongs(getString(R.string.Songs), songList)
                Log.d("Playing", "" + deletionProcess)
                if (deletionProcess) {
                    musicSrv!!.setList(songList)
                    if (!songList.contains(playingSong)) {
                        setSongPosition(songPosn)
                        setPlayingSong(songList[songPosn])
                    } else {
                        val songIndex = songList.indexOf(playingSong)
                        setSongPosition(songIndex)
                        setPlayingSong(songList[songIndex])
                    }
                    deletionProcess = false
                }

            }
        }

        viewModel.getPlayingSong().observe(this) { song ->
            playingSong = song
            Log.d("Playing", "Playing Song " + playingSong!!.title)

            binding.playingPanel.txtSongName.text = playingSong!!.title
            binding.playingPanel.txtSongArtist.text = playingSong!!.artist
            binding.playingPanel.totalDuration.text = String.format(
                "%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(playingSong!!.duration),
                TimeUnit.MILLISECONDS.toSeconds(playingSong!!.duration) -
                        TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                playingSong!!.duration
                            )
                        )
            )

            setSongImageOnImageView()

            checkInFav(playingSong)

            if (played) {
                manage.putStringPref(getString(R.string.position), songPosn.toString())
                manage.putStringPref(getString(R.string.ID), songPosn.toString())
            }
        }

        viewModel.getSongPosition().observe(this) { integer ->
            songPosn = integer
            if (songPosn >= songList.size) {
                songPosn = 0
            }
            musicSrv!!.setSong(songPosn)
            Log.d("Playing", "Position Changed $songPosn")
        }

        lastSongId = manage.getStringPref(getString(R.string.ID))
        lastShuffle = manage.getBooleanPref(getString(R.string.Shuffle))
        lastRepeat = manage.getBooleanPref(getString(R.string.Repeat))
        lastRepeatOne = manage.getBooleanPref(getString(R.string.RepeatOne))
        playingDuration = manage.getStringPref(getString(R.string.currentPlayingDuration))

        var start = manage.getBooleanPref(getString(R.string.Started))
        val position = manage.getStringPref(getString(R.string.position))

        if (manage.getBooleanPref(getString(R.string.Songs))) {
            songList.clear()
            normalList.clear()
            songList.addAll(viewModel.getTinyDbSongs(getString(R.string.Songs)))
            normalList.addAll(viewModel.getTinyDbSongs(getString(R.string.NormalSongs)))
            Log.d("Playing", songList.size.toString() + "")
            setPlayingList(songList)
            if (position != null) {
                when {
                    songList.size == 0 -> {
                        start = false
                    }
                    songList.size <= position.toInt() -> {
                        songPosn = 0
                        setSongPosition(songPosn)
                    }
                    else -> {
                        songPosn = position.toInt()
                        setSongPosition(songPosn)
                    }
                }
            }
        }

        started = start

        if (lastSongId.isNotBlank() || lastSongId.isNotEmpty()) {
            songPosn = lastSongId.toInt()
            if (songList.size != 0) {
                if (songList.size <= songPosn) {
                    setSongPosition(0)
                    setPlayingSong(songList[0])
                } else {
                    setSongPosition(songPosn)
                    setPlayingSong(songList[songPosn])
                }
            }
        }

        if (lastShuffle) {
            binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle)
        } else {
            binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
        }

        if (lastRepeat) {
            if (lastRepeatOne) {
                binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
            } else {
                binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_all)
            }
        } else {
            binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
        }

        viewModel.getPlaylistSong(getString(R.string.favTracks)).observe(this) { playlists ->
            var check = false
            if (started) {
                for ((_, _, song) in playlists) {
                    check = song == playingSong
                }
            }
            if (check) {
                binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav)
            } else {
                binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav_not)
            }
        }

        binding.slidingLayout.apply {
            addPanelSlideListener(onSlideListener())
            getChildAt(1).setOnClickListener(null)
        }
        binding.playingPanel.apply {
            btnPlayPauseSlide.setOnClickListener(this@MainActivity)
            btnPlayPause.setOnClickListener(this@MainActivity)
            btnNext.setOnClickListener(this@MainActivity)
            btnPrev.setOnClickListener(this@MainActivity)
            btnShuffle.setOnClickListener(this@MainActivity)
            btnRepeat.setOnClickListener(this@MainActivity)
            slidePanelTop.setOnClickListener(this@MainActivity)
            imgEq.setOnClickListener(this@MainActivity)
            imgPlaylist.setOnClickListener(this@MainActivity)
            imgFav.setOnClickListener(this@MainActivity)
        }

        binding.fabShuffle.setOnClickListener {
            shuffleLibrarySongs()
        }

        loadBanner1()

        mUpdateManager = UpdateManager.Builder(this).mode(UpdateManager.FLEXIBLE)
        mUpdateManager?.start()

        mUpdateManager?.onResume()

        AppRate(this)
            .setShowIfAppHasCrashed(false)
            .setMinDaysUntilPrompt(10)
            .setMinLaunchesUntilPrompt(15)
            .init()
    }

    private fun loadBanner1() {
        adView = AdView(this)
        adView!!.adUnitId = getString(R.string.mainActId)
        binding.playingPanel.bannerContainerPlayer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView!!.adSize = adSize
        adView!!.loadAd(adRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if ((requestCode == SEARCH_RESULT) && data != null) {
                val position = data.getIntExtra("position", -1)
                val songsArrayList: ArrayList<Song> = data.getParcelableArrayListExtra("songList")!!
                onFragmentItemClick(position, songsArrayList, false)
            }

            if (requestCode != SEARCH_RESULT) {
                if (DELETE_URI != null) {
                    val file = File(DELETE_URI!!.path!!)
                    if (file.exists()) {
                        file.delete()
                        if (file.exists()) {
                            try {
                                file.canonicalFile.delete()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    contentResolver.delete(DELETE_URI!!, null, null)
                    DELETE_URI = null
                    for (fragment in supportFragmentManager.fragments) {
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }

    override fun onFragmentItemClick(
        position: Int,
        songsArrayList: ArrayList<Song>,
        nowPlaying: Boolean
    ) {
        songList = songsArrayList.clone() as ArrayList<Song>
        songPosn = position
        if (!nowPlaying) {
            normalList.clear()
            binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            musicSrv!!.shuffle = false
            setRepeatOff()
            manage.putBooleanPref(getString(R.string.Shuffle), false)
        }

        setPlayingList(songList)
        setSongPosition(songPosn)
        setPlayingSong(songList[songPosn])

        playingDuration = "0"

        musicSrv!!.setPlayingPosition(playingDuration)
        musicSrv!!.setList(songList)
        musicSrv!!.setSong(songPosn)
        musicSrv!!.playSong()

        played = true
        started = true
        setPauseIcons()
    }

    fun btnPlayPause() {
        if (musicSrv!!.isSongPlaying) {
            pause()
        } else {
            if (!played) {
                musicSrv!!.playSong()
            }
            start()
            played = true
        }
    }

    override fun onClick(v: View) {
        if (started) {
            if (v == binding.playingPanel.imgEq) {
                val eq = Intent(this@MainActivity, EqualizerActivity::class.java)
                startActivity(eq)
            }
            if (v == binding.playingPanel.btnPlayPauseSlide || v == binding.playingPanel.btnPlayPause) {
                btnPlayPause()
            }
            if (v == binding.playingPanel.btnNext) {
                playNext()
            }
            if (v == binding.playingPanel.btnPrev) {
                playPrev()
            }
            if (v == binding.playingPanel.btnShuffle) {
                musicSrv!!.shuffleOnOff()
                lastShuffle = if (musicSrv!!.shuffle) {
                    normalList.clear()
                    normalList.addAll(songList)
                    songList.shuffle()

                    binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle)
                    setRepeatOff()

                    Toast.makeText(this, getString(R.string.Shuffle_On), Toast.LENGTH_SHORT).show()
                    true
                } else {
                    if (songList.size != normalList.size) {
                        val songIterator = normalList.iterator()
                        while (songIterator.hasNext()) {
                            val song = songIterator.next()
                            if (!songList.contains(song)) {
                                songIterator.remove()
                            }
                        }
                    }
                    songList.removeAll(normalList)
                    songList.addAll(normalList)
                    normalList.clear()

                    Log.i("POSITION", songPosn.toString() + " " + musicSrv!!.songPosn)
                    binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)

                    Toast.makeText(this, getString(R.string.Shuffle_Off), Toast.LENGTH_SHORT).show()
                    false
                }
                manage.putBooleanPref(getString(R.string.Shuffle), lastShuffle)
                Log.d(TAG, playingSong!!.title)
                songPosn = songList.indexOf(playingSong)
                setPlayingList(songList)
                setSongPosition(songPosn)
                setPlayingSong(songList[songPosn])
            }
            if (v == binding.playingPanel.btnRepeat) {
                musicSrv!!.setRepeat()
                if (musicSrv!!.repeat && musicSrv!!.repeat_one) {
                    lastRepeatOne = true
                    lastRepeat = true

                    Toast.makeText(this, getString(R.string.Repeat_One), Toast.LENGTH_SHORT).show()
                    binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                } else if (musicSrv!!.repeat) {
                    lastRepeat = true
                    lastRepeatOne = false

                    Toast.makeText(this, getString(R.string.Repeat_On), Toast.LENGTH_SHORT).show()
                    binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_all)
                } else {
                    setRepeatOff()
                    Toast.makeText(this, getString(R.string.Repeat_Off), Toast.LENGTH_SHORT).show()
                }

                manage.putBooleanPref(getString(R.string.Repeat), lastRepeat)
                manage.putBooleanPref(getString(R.string.RepeatOne), lastRepeatOne)
            }
            if (v == binding.playingPanel.imgPlaylist) {
                binding.slidingLayout.panelState = PanelState.COLLAPSED
                if (supportFragmentManager.findFragmentByTag("NowPlayingFragment") != null) {
                    return
                }
                hideHomePanel()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.apply {
                    replace(R.id.container, NowPlayingFragment(), "NowPlayingFragment")
                    addToBackStack(null)
                    commit()
                }
            }
            if (v == binding.playingPanel.imgFav) {
                addToFavPlaylist()
            }
        } else {
            Toast.makeText(this, getString(R.string.playAsong), Toast.LENGTH_SHORT).show()
        }
        if (v == binding.playingPanel.slidePanelTop) {
            if (binding.slidingLayout.panelState == PanelState.COLLAPSED) {
                binding.slidingLayout.panelState = PanelState.EXPANDED
            } else if (binding.slidingLayout.panelState == PanelState.EXPANDED) {
                binding.slidingLayout.panelState = PanelState.COLLAPSED
            }
        }
    }

    private fun setRepeatOff() {
        musicSrv!!.player.isLooping = false
        musicSrv!!.repeat = false
        musicSrv!!.repeat_one = false

        lastRepeatOne = false
        lastRepeat = false

        binding.playingPanel.btnRepeat.setImageResource(R.drawable.ic_repeat_off)

        manage.putBooleanPref(getString(R.string.Repeat), lastRepeat)
        manage.putBooleanPref(getString(R.string.RepeatOne), lastRepeatOne)
    }

    fun addToFavPlaylist() {
        try {
            val song = songList[songPosn]
            val exist = viewModel.isSongExist(getString(R.string.favTracks), song.id)
            if (exist > 0) {
                musicSrv!!.updateFavNotification(false)

                binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav_not)

                viewModel.removeSong(getString(R.string.favTracks), song.id)
            } else {
                val playlist = Playlist(0, getString(R.string.favTracks), song)
                viewModel.addSong(playlist)

                musicSrv!!.updateFavNotification(true)

                binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav)

                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.added_To_Favorite),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ignored: Exception) {
        }
    }

    private fun checkInFav(song: Song?) {
        val exist = viewModel.isSongExist(getString(R.string.favTracks), song!!.id)
        if (exist > 0) {
            binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav)
            musicSrv!!.updateFavNotification(true)
        } else {
            binding.playingPanel.imgFav.setImageResource(R.drawable.ic_fav_not)
            musicSrv!!.updateFavNotification(false)
        }
    }

    private fun onSlideListener(): SlidingUpPanelLayout.PanelSlideListener {
        return object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                Log.e("OffsetData", "$slideOffset")
                if (started) {
                    binding.playingPanel.playerController.alpha = slideOffset
                }
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                if (started) {
                    if (newState == PanelState.COLLAPSED) {
                        binding.playingPanel.txtSongName.isSelected = false
                        binding.playingPanel.txtSongArtist.isSelected = false

                        binding.playingPanel.btnPlayPauseSlide.visibility = View.VISIBLE

                        if (songList.size > 0) {
                            showSlideImage()
                        } else {
                            hideSlideImage()
                        }
                    } else if (newState == PanelState.EXPANDED) {
                        if (musicSrv!!.player.isPlaying) {
                            binding.playingPanel.txtSongName.isSelected = true
                            binding.playingPanel.txtSongArtist.isSelected = true
                        } else {
                            binding.playingPanel.txtSongName.isSelected = false
                            binding.playingPanel.txtSongArtist.isSelected = false
                        }
                        binding.playingPanel.btnPlayPauseSlide.visibility = View.GONE
                        hideSlideImage()
                    }
                }
            }
        }
    }

    private fun setSongImageOnImageView() {
        val art = Uri.parse(playingSong!!.art)
        binding.playingPanel.controlBack.load(art) {
            error(R.drawable.logo)
            crossfade(true)
        }

        binding.playingPanel.cardImage.load(art) {
            error(R.drawable.logo)
            transformations(RoundedCornersTransformation(20f))
            crossfade(true)
        }

        binding.playingPanel.slideImage.load(art) {
            error(R.drawable.logo)
            transformations(RoundedCornersTransformation(10f))
            crossfade(true)
        }

        if (binding.slidingLayout.panelState == PanelState.COLLAPSED) {
            showSlideImage()
        } else {
            binding.playingPanel.btnPlayPauseSlide.visibility = View.GONE
            hideSlideImage()
        }
    }

    fun hideHomePanel() {
        binding.container.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
    }

    private fun showHomePanel() {
        supportFragmentManager.popBackStack()
        binding.container.visibility = View.GONE
        binding.mainLayout.visibility = View.VISIBLE
    }

    private fun showSlideImage() {
        binding.playingPanel.slideImage.visibility = View.VISIBLE
        binding.playingPanel.slideImage2.visibility = View.INVISIBLE
    }

    private fun hideSlideImage() {
        binding.playingPanel.slideImage.visibility = View.INVISIBLE
        binding.playingPanel.slideImage2.visibility = View.VISIBLE
    }

    fun setPlayIcons() {
        binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp)
        binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp)
    }

    fun setPauseIcons() {
        binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //menu item selected
        when (item.itemId) {
            R.id.timer -> showTimer()
            R.id.equalizer -> {
                val eq = Intent(this@MainActivity, EqualizerActivity::class.java)
                startActivity(eq)
            }
            R.id.search -> {
                val i = Intent(this@MainActivity, SearchActivity::class.java)
                startActivityForResult(i, SEARCH_RESULT)
            }
            R.id.rescan -> {
                val scan = SongsProvider()
                scan.fetchAllData(contentResolver).observe(this) { aBoolean ->
                    if (aBoolean) {
                        showSnackBar(getString(R.string.mediaScan))
                    }
                }
            }
            R.id.share -> try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player")
                var shareMessage = "\nDownload the light weight Playback Music Player app\n\n"
                shareMessage =
                    "${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
            R.id.rateUs -> launchMarket()
            R.id.suggestion -> {
                val email = Intent(Intent.ACTION_SEND)
                email.putExtra(Intent.EXTRA_EMAIL, arrayOf("sharmaravi.23960@gmail.com"))
                email.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player Suggestion")
                email.putExtra(Intent.EXTRA_TEXT, "")
                email.type = "message/rfc822"
                startActivity(Intent.createChooser(email, "Choose an Email client :"))
            }
            R.id.about -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchMarket() {
        try {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        } catch (e: ActivityNotFoundException) {
            showSnackBar(getString(R.string.unableToFindMarketApp))
        }

        manage.putBooleanPref(PrefsContract.PREF_DONT_SHOW_AGAIN, true)
    }

    private fun showTimer() {
        val timerArray: Array<String> = resources.getStringArray(R.array.timer)
        val trackArray: Array<String> = resources.getStringArray(R.array.tracks)
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
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
            seekValue = if (timerSelectedValue) {
                timerArray
            } else {
                trackArray
            }
            switchTimerAlertView(
                timerSelectedValue,
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
        dialogBinding.txtTracks.setOnClickListener {
            seekValue = trackArray
            switchTimerAlertView(false, dialogBinding.txtTimer, dialogBinding.txtTracks)
            dialogBinding.alertSeekBar.progress = 0
            dialogBinding.txtSeekValue.text = seekValue[0].toString()
        }
        dialogBinding.timerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                TIMER = false
                dialogBinding.txtOnOff.text = "Off"
                dialogBinding.timerBlocker.visibility = View.VISIBLE
                alertDialog.setCancelable(true)
                if (am != null && pi != null) {
                    am!!.cancel(pi)
                    am = null
                    pi = null
                    showSnackBar(getString(R.string.timeOff))
                }
            } else {
                TIMER = true
                dialogBinding.txtOnOff.text = "On"
                dialogBinding.timerBlocker.visibility = View.GONE
                alertDialog.setCancelable(false)
            }
        }
        dialogBinding.txtSave.setOnClickListener {
            if (timerSelectedValue) {
                when (alert_current_value) {
                    0 -> {
                        setTimer(15 * 60 * 1000)
                        showSnackBar(getString(R.string.mins15))
                    }
                    1 -> {
                        setTimer(30 * 60 * 1000)
                        showSnackBar(getString(R.string.mins30))
                    }
                    2 -> {
                        setTimer(45 * 60 * 1000)
                        showSnackBar(getString(R.string.mins45))
                    }
                    3 -> {
                        setTimer(60 * 60 * 1000)
                        showSnackBar(getString(R.string.mins60))
                    }
                    4 -> {
                        setTimer(90 * 60 * 1000)
                        showSnackBar(getString(R.string.mins90))
                    }
                    5 -> {
                        setTimer(120 * 60 * 1000)
                        showSnackBar(getString(R.string.mins120))
                    }
                }
            }
            alertDialog.dismiss()
        }
    }

    fun trackCounterCheck() {
        if (TIMER && !timerSelectedValue) {
            if (trackCount == alert_current_value) {
                trackCount = 0
                stopApp()
            } else {
                trackCount++
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun switchTimerAlertView(timerSelected: Boolean, timer: TextView, tracks: TextView) {
        timerSelectedValue = timerSelected
        if (timerSelected) {
            timer.setTextColor(resources.getColor(R.color.popupItemBackground))
            tracks.setTextColor(resources.getColor(R.color.white))
            timer.background = getDrawable(R.drawable.timer_alert_tab_selected_left)
            tracks.background = getDrawable(R.drawable.timer_alert_tab_unselected_right)
        } else {
            tracks.setTextColor(resources.getColor(R.color.popupItemBackground))
            timer.setTextColor(resources.getColor(R.color.white))
            tracks.background = getDrawable(R.drawable.timer_alert_tab_selected_right)
            timer.background = getDrawable(R.drawable.timer_alert_tab_unselected_left)
        }
    }

    private fun setTimer(time: Int) {
        val i = Intent(this, Timer::class.java)
        pi = PendingIntent.getBroadcast(this, 1234, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am = getSystemService(ALARM_SERVICE) as AlarmManager
        am!![AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time] = pi
    }

    //play next
    fun playNext() {
        if (songList.size > 0) {
            playingDuration = "0"
            musicSrv!!.setPlayingPosition(playingDuration)
            musicSrv!!.playNext()
            played = true
            setPauseIcons()
            songPosn = musicSrv!!.songPosn
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show()
        }
    }

    //play previous
    fun playPrev() {
        if (songList.size > 0) {
            playingDuration = "0"
            musicSrv!!.setPlayingPosition(playingDuration)
            musicSrv!!.playPrev()
            played = true
            setPauseIcons()
            songPosn = musicSrv!!.songPosn
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (binding.slidingLayout.panelState == PanelState.EXPANDED ||
            binding.slidingLayout.panelState == PanelState.ANCHORED
        ) {
            binding.slidingLayout.panelState = PanelState.COLLAPSED
        } else {
            if (binding.container.visibility == View.VISIBLE) {
                showHomePanel()
                return
            }
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.setCurrentItem(0, true)
            } else if (musicSrv != null && musicSrv!!.isSongPlaying) {
                moveTaskToBack(true)
            } else {
                if (doubleBackToExitPressedOnce) {
                    stopApp()
                    return
                }
                doubleBackToExitPressedOnce = true
                showSnackBar(getString(R.string.tapToExit))

                Handler(Looper.getMainLooper()).postDelayed(
                    { doubleBackToExitPressedOnce = false },
                    2000
                )
            }
        }
    }

    private fun killApp() {
        if (musicSrv != null) {
            if (played) {
                manage.putStringPref(getString(R.string.ID), songPosn.toString())
                manage.putBooleanPref(getString(R.string.Started), started)
                manage.putBooleanPref(getString(R.string.Songs), true)
                manage.putStringPref(getString(R.string.position), songPosn.toString())
                manage.putStringPref(
                    getString(R.string.currentPlayingDuration),
                    musicSrv!!.player.currentPosition.toString()
                )
                unbindService(musicConnection)
            }
        }

        if (TIMER && timerSelectedValue) {
            am!!.cancel(pi)
        }
        if (mediaSession != null) {
            mediaSession!!.release()
        }
        saveEqualizerSettings()
        unRegisterMediaChangeObserver()
        musicSrv = null
        playIntent = null
        if (adView != null) {
            adView!!.destroy()
        }
        finish()
    }

    fun stopApp() {
        killApp()
        Handler(Looper.getMainLooper()).postDelayed({ exitProcess(0) }, 800)
    }

    public override fun onDestroy() {
        killApp()
        super.onDestroy()
    }

    private fun start() {
        musicSrv!!.go()
        if (binding.slidingLayout.panelState == PanelState.EXPANDED) {
            binding.playingPanel.txtSongName.isSelected = true
            binding.playingPanel.txtSongArtist.isSelected = true
        } else if (binding.slidingLayout.panelState == PanelState.COLLAPSED) {
            binding.playingPanel.txtSongName.isSelected = false
            binding.playingPanel.txtSongArtist.isSelected = false
        }
        setPauseIcons()
        musicSrv!!.mProgressRunner.run()
    }

    private fun pause() {
        binding.playingPanel.txtSongName.isSelected = false
        binding.playingPanel.txtSongArtist.isSelected = false
        musicSrv!!.pausePlayer()
        setPlayIcons()
    }

    //Headset button listener
    /*
     * onKeyDown will work when Activity is in Foreground
     * */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            //handle click
            val action = event.action
            if (action == KeyEvent.ACTION_DOWN) {
                performAction(action)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun performAction(action: Int) {
        if (action == KeyEvent.ACTION_DOWN) {
            d++
            val handler = Handler(Looper.getMainLooper())
            val r = Runnable {
                if (d == 1) {
                    btnPlayPause()
                }
                if (d == 2) {
                    playNext()
                }
                if (d == 3) {
                    playPrev()
                }
                d = 0
            }
            if (d == 1) {
                handler.postDelayed(r, 500)
            }
        }
    }

    /*
     * This will work when activity is in background
     * */
    private fun initMediaSessions() {
        mediaSession = MediaSession(applicationContext, TAG!!)
        mediaSession!!.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        val state = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(
                if (musicSrv!!.player.isPlaying) PlaybackState.STATE_PAUSED else PlaybackState.STATE_PLAYING,
                PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f
            )
            .build()
        mediaSession!!.setPlaybackState(state)
        mediaSession!!.setMetadata(
            MediaMetadata.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, -1)
                .build()
        )
        mediaSession!!.setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                super.onPlay()
                btnPlayPause()
            }

            override fun onPause() {
                super.onPause()
                btnPlayPause()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                playNext()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                playPrev()
            }
        })
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentActivity?) :
        FragmentStateAdapter(
            fm!!
        ) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PlaylistFragment()
                1 -> NameWise()
                2 -> AlbumsFragment()
                3 -> ArtistFragment()
                else -> PlaylistFragment()
            }
        }

        override fun getItemCount(): Int {
            return 4
        }
    }

    //connect to the service
    private val musicConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.e(TAG, "onServiceConnected: Service Connected")
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass alert_list
            if (lastSongId.isNotBlank() || lastSongId.isNotEmpty()) {
                setPlayingList(songList)
                musicSrv!!.setShuffle(lastShuffle)
                musicSrv!!.checkRepeat(lastRepeat, lastRepeatOne)
                setSongPosition(lastSongId.toInt())
            }
            musicSrv!!.setUIControls(
                binding.playingPanel.seekBar,
                binding.playingPanel.currentPosition,
                binding.playingPanel.totalDuration
            )
            loadEqualizerSettings()
            initMediaSessions()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "onServiceDisconnected: Service Disconnected")
            killApp()
        }
    }

    //Check of app update

    /*internal inner class CheckUpdate : CoroutinesAsyncTask<Void?, Void?, Void?>("UpdateCheck") {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                latestVersion = Jsoup
                    .connect(
                        "https://play.google.com/store/apps/details?id="
                                + packageName
                    )
                    .timeout(30000)
                    .get()
                    .select(
                        "div.hAyfc:nth-child(4)>" +
                                "span:nth-child(2) > div:nth-child(1)" +
                                "> span:nth-child(1)"
                    )
                    .first()
                    .ownText()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch(e: Exception) {
                Log.e("Excpetion", e.toString())
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentVersion = BuildConfig.VERSION_NAME
            if (latestVersion != null) {
                var currentVer = versionStringToLong(currentVersion!!)
                var latestVer = versionStringToLong(latestVersion!!)
                val len =
                    if (currentVer.length > latestVer.length) currentVer.length - latestVer.length else latestVer.length - currentVer.length
                val sb = StringBuilder()
                for (i in 0 until len) {
                    sb.append("0")
                }
                if (currentVer.length > latestVer.length) {
                    latestVer += sb.toString()
                } else {
                    currentVer += sb.toString()
                }
                val cVersion = currentVer.toLong()
                val lVersion = latestVer.toLong()
                if (lVersion > cVersion) {
                    updateAlertDialog()
                }
            }
        }
    }

    private fun versionStringToLong(version: String): String {
//        val split = version.split("\\.")
        val split = Pattern.compile("\\.").split(version)
        val sb = StringBuilder()
        for (s in split) {
            sb.append(s)
        }
        return sb.toString()
    }

    private fun updateAlertDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle(getString(R.string.newUpdate))
        builder.setMessage(getString(R.string.updateMessage, currentVersion, latestVersion))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.updateButton)) { dialog, which ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Play Store Not Found", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.updateLater)) { dialog, which -> dialog.dismiss() }
        val ad = builder.create()
        ad.show()
    }*/

    private fun saveEqualizerSettings() {
        try {
            if (Settings.equalizerModel != null) {
                val settings = EqualizerSettings()
                settings.bassStrength = Settings.equalizerModel.bassStrength
                settings.virtualizerStrength = Settings.equalizerModel.virtualizerStrength
                settings.presetPos = Settings.equalizerModel.presetPos
                settings.reverbPreset = Settings.equalizerModel.reverbPreset
                settings.seekbarpos = Settings.equalizerModel.seekbarpos
                settings.isEqualizerEnabled = Settings.equalizerModel.isEqualizerEnabled
                settings.isEqualizerReloaded = Settings.equalizerModel.isEqualizerReloaded
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                val gson = Gson()
                preferences.edit()
                    .putString(PREF_KEY, gson.toJson(settings))
                    .apply()
            }
            if (mEqualizer != null) {
                mEqualizer!!.release()
                mEqualizer = null
            }
            if (bassBoost != null) {
                bassBoost!!.release()
                bassBoost = null
            }
            if (virtualizer != null) {
                virtualizer!!.release()
                virtualizer = null
            }
            if (presetReverb != null) {
                presetReverb!!.release()
                presetReverb = null
            }
        } catch (exp: Exception) {

        }
    }

    private fun loadEqualizerSettings() {
        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val gson = Gson()
            val settings =
                gson.fromJson(preferences.getString(PREF_KEY, "{}"), EqualizerSettings::class.java)
            val model = EqualizerModel()
            model.isEqualizerEnabled = settings.isEqualizerEnabled
            model.isEqualizerReloaded = settings.isEqualizerReloaded
            model.bassStrength = settings.bassStrength
            model.virtualizerStrength = settings.virtualizerStrength
            model.presetPos = settings.presetPos
            model.reverbPreset = settings.reverbPreset
            model.seekbarpos = settings.seekbarpos
            Settings.isEqualizerEnabled = settings.isEqualizerEnabled
            Settings.isEqualizerReloaded = settings.isEqualizerReloaded
            Settings.bassStrength = settings.bassStrength
            Settings.virtualizerStrength = settings.virtualizerStrength
            Settings.presetPos = settings.presetPos
            Settings.reverbPreset = settings.reverbPreset
            Settings.seekbarpos = settings.seekbarpos
            Settings.equalizerModel = model
            if (Settings.equalizerModel == null) {
                Settings.equalizerModel = EqualizerModel()
                Settings.equalizerModel.reverbPreset = PresetReverb.PRESET_NONE
                Settings.equalizerModel.bassStrength = (1000 / 19).toShort()
                Settings.equalizerModel.virtualizerStrength = (1000 / 19).toShort()
            }
            if (mEqualizer != null) {
                mEqualizer!!.release()
                mEqualizer = null
            }
            if (bassBoost != null) {
                bassBoost!!.release()
                bassBoost = null
            }
            if (virtualizer != null) {
                virtualizer!!.release()
                virtualizer = null
            }
            if (presetReverb != null) {
                presetReverb!!.release()
                presetReverb = null
            }
            mEqualizer = Equalizer(0, sessionId)
            bassBoost = BassBoost(0, sessionId)
            presetReverb = PresetReverb(0, sessionId)
            virtualizer = Virtualizer(0, sessionId)
            val bassBoostSettingTemp = bassBoost!!.properties
            val bassBoostSetting = BassBoost.Settings(bassBoostSettingTemp.toString())
            bassBoostSetting.strength = Settings.equalizerModel.bassStrength
            bassBoost!!.properties = bassBoostSetting
            val virtualizerSettingTemp = virtualizer!!.properties
            val virtualizerSetting = Virtualizer.Settings(virtualizerSettingTemp.toString())
            virtualizerSetting.strength = Settings.equalizerModel.virtualizerStrength
            virtualizer!!.properties = virtualizerSetting
            presetReverb!!.preset = Settings.equalizerModel.reverbPreset
            mEqualizer!!.enabled = Settings.isEqualizerEnabled
            bassBoost!!.enabled = Settings.isEqualizerEnabled
            virtualizer!!.enabled = Settings.isEqualizerEnabled
            presetReverb!!.enabled = Settings.isEqualizerEnabled
            try {
                if (Settings.presetPos == 0) {
                    for (bandIdx in 0 until mEqualizer!!.numberOfBands) {
                        mEqualizer!!.setBandLevel(
                            bandIdx.toShort(), Settings.seekbarpos[bandIdx]
                                .toShort()
                        )
                    }
                } else {
                    mEqualizer!!.usePreset(Settings.presetPos.toShort())
                }
            } catch (e: Exception) {
                Settings.presetPos = 0
                var bandIdx: Short = 0
                while (bandIdx < mEqualizer!!.numberOfBands) {
                    mEqualizer!!.setBandLevel(
                        bandIdx, Settings.seekbarpos[bandIdx.toInt()]
                            .toShort()
                    )
                    bandIdx++
                }
            }
        } catch (exp: Exception) {
            menu?.let { menu ->
                val menuItem = menu.findItem(R.id.equalizer)
                menuItem.isVisible = false
            }
        }
    }

    private var observer: ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val provider = SongsProvider()
                provider.fetchAllData(contentResolver).observe(this@MainActivity) { aBoolean ->
                    if (aBoolean) {
                        checkInPlaylists()
                    }
                }
            }

        }

    private fun registerMediaChangeObserver() {
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true, observer
        )
    }

    private fun unRegisterMediaChangeObserver() {
        contentResolver.unregisterContentObserver(observer)
    }

    private fun checkInPlaylists() {
        if (songListByName.value != null) {
            val songListByName = songListByName.value!!
            if (songListByName.size <= 1) {
                manage.putStringPref(getString(R.string.ID), "remove")
                manage.putBooleanPref(getString(R.string.Shuffle), false)
                manage.putBooleanPref(getString(R.string.Repeat), false)
                manage.putBooleanPref(getString(R.string.RepeatOne), false)
                manage.putBooleanPref(getString(R.string.Started), false)
                manage.putBooleanPref(getString(R.string.Songs), false)
                manage.putStringPref("position", "remove")
            }
            if (songListByName.size > 0) {
                val playListArrayList: MutableList<String> = ArrayList()
                playListArrayList.add("NormalSongs")
                playListArrayList.add("Songs")

                for (playListName in playListArrayList) {
                    val songList = tinydb.getListObject(playListName, Song::class.java)
                    val iterator = songList.iterator()
                    while (iterator.hasNext()) {
                        val value = iterator.next()
                        if (!songListByName.contains(value)) {
                            iterator.remove()
                        }
                    }
                    if (playListName == "Songs" && songList.size == 0) {
                        manage.putBooleanPref(getString(R.string.Songs), false)
                    }
                    tinydb.putListObject(playListName, songList)
                }

                viewModel.removeSongFromPlaylist()

                if (viewModel.getPlayingList().value != null) {
                    val songsToRemove = ArrayList<Song>()
                    for (s in viewModel.getPlayingList().value!!) {
                        if (!songListByName.contains(s)) {
                            songsToRemove.add(s)
                        }
                    }
                    for (s in songsToRemove) {
                        removeFromPlayingList(s)
                    }
                }

                lastPlayedRepository.getLastPlayedSongsList()
                    .observe(this@MainActivity) { lastPlayed: List<LastPlayed>? ->
                        if (lastPlayed != null) {
                            for ((s) in lastPlayed) {
                                if (!songListByName.contains(s)) {
                                    lastPlayedRepository.deleteSongFromLastPlayed(s.id)
                                }
                            }
                        }
                    }

                mostPlayedRepository.getMostPlayedSongs()
                    .observe(this@MainActivity) { mostPlayed: List<MostPlayed>? ->
                        if (mostPlayed != null) {
                            for ((s) in mostPlayed) {
                                if (!songListByName.contains(s)) {
                                    mostPlayedRepository.deleteMostPlayedSong(s.id)
                                }
                            }
                        }
                    }
            } else {
                started = false
                showNoSongFoundDialog()
            }
        } else {
            started = false
            showNoSongFoundDialog()
        }
    }

    private fun showNoSongFoundDialog() {
        try {
            val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
            val dialogBinding: AlertPopupMessageBinding =
                AlertPopupMessageBinding.inflate(layoutInflater)
            dialog.setView(dialogBinding.root)

            val alertDialog = dialog.create()
            alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.setCancelable(false)

            dialogBinding.txtTitle.text = getString(R.string.noFileFound)
            dialogBinding.txtMessage.text = getString(R.string.noFileFoundMessage)
            dialogBinding.txtSave.text = getString(R.string.noFileFoundButton)

            dialogBinding.txtSave.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()
        } catch (exp: Exception) {

        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.slidingLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun shuffleLibrarySongs() {
        songListByName.value?.let { songsList ->
            if (songsList.size > 0) {

                playingDuration = "0"
                musicSrv!!.setPlayingPosition(playingDuration)
                musicSrv!!.setShuffle(true)

                lastShuffle = true

                binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle)

                normalList.clear()
                songList.clear()

                normalList.addAll(songListByName.value!!)
                songList.addAll(songListByName.value!!)
                songList.shuffle()

                manage.putBooleanPref(getString(R.string.Shuffle), lastShuffle)

                songPosn = 0

                setPlayingList(songList)
                setSongPosition(songPosn)
                setPlayingSong(songList[songPosn])

                musicSrv!!.playSong()

                played = true
                started = true
                setPauseIcons()
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.noSongFoundToPlay), Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this@MainActivity, getString(R.string.noSongFoundToPlay), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREF_KEY = "equalizer"
        var d = 0
        const val SEARCH_RESULT = 2

        var instance: MainActivity? = null
            private set
    }

    // Declare the UpdateManager
    var mUpdateManager: UpdateManager? = null

}