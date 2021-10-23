package com.ravisharma.playbackmusic

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
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
import com.ravisharma.playbackmusic.activities.NowPlayingActivity
import com.ravisharma.playbackmusic.activities.SearchActivity
import com.ravisharma.playbackmusic.broadcast.Timer
import com.ravisharma.playbackmusic.database.model.LastPlayed
import com.ravisharma.playbackmusic.database.model.MostPlayed
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.databinding.ActivityMainBinding
import com.ravisharma.playbackmusic.databinding.AlertTimerBinding
import com.ravisharma.playbackmusic.equalizer.model.EqualizerModel
import com.ravisharma.playbackmusic.equalizer.model.EqualizerSettings
import com.ravisharma.playbackmusic.equalizer.model.Settings
import com.ravisharma.playbackmusic.fragments.*
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import com.ravisharma.playbackmusic.provider.SongsProvider
import com.ravisharma.playbackmusic.provider.SongsProvider.Companion.songListByName
import com.ravisharma.playbackmusic.utils.*
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.lang.Runnable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), View.OnClickListener, NameWise.OnFragmentItemClicked,
    CategorySongFragment.OnFragmentItemClicked {

    private var TAG: String? = null

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainActivityViewModel

    private var adView: AdView? = null
    private var latestVersion: String? = null
    private var currentVersion: String? = null

    @JvmField
    var sessionId = 0
    private lateinit var lastSongId: String
    private var playingDuration: String? = null
    private var lastShuffle = false
    private var lastRepeat = false
    private var lastRepeatOne = false
    
    var musicSrv: MusicService? = null

    var musicBound = false
    var serviceInitialized = false
    var fromList = false
    var started = false
    var deletionProcess = false
    var fromButton = false

    @JvmField
    var played = false
    var playbackPaused = false
    var TIMER = false
    private var doubleBackToExitPressedOnce = false

    var songList: ArrayList<Song> = ArrayList()
    var normalList: ArrayList<Song> = ArrayList()

    @JvmField
    var songPosn = 0
    private var playIntent: Intent? = null
    private var pi: PendingIntent? = null

    private var am: AlarmManager? = null

    private lateinit var manage: PrefManager
    private lateinit var tinydb: TinyDB
    private lateinit var repository: PlaylistRepository

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instance = this
        TAG = getString(R.string.app_name)

        binding.toolbar.title = getString(R.string.app_name)
        binding.toolbar.setTitleTextColor(resources.getColor(R.color.titleColor))

        setSupportActionBar(binding.toolbar)

        setUpView()

        registerMediaChangeObserver()
    }

    private fun setUpView() {
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE)
        }
        musicSrv = MusicService()
        binding.playingPanel.playerController.visibility = View.INVISIBLE
        
        val sectionsPagerAdapter = SectionsPagerAdapter(this)

        binding.viewPager.apply {
            offscreenPageLimit = 2
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

        manage = PrefManager(applicationContext)
        tinydb = TinyDB(this)
        repository = PlaylistRepository(this)

        val viewModelFactory = MainViewModelFactory(repository, tinydb)

        viewModel = ViewModelProvider(this, viewModelFactory).get(
            MainActivityViewModel::class.java
        )

        getPlayingListData().observe(this, { songs ->
            songList = songs
            if (songList.size > 0) {
                Log.d("Playing", "List Changed " + songList.size)
                musicSrv!!.setList(songList)
                if (playingDuration != null) {
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
                    }
                    deletionProcess = false
                }
            }
        })

        viewModel.getPlayingSong().observe(this, { song ->
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

            val manage = PrefManager(this@MainActivity)
            manage.storeInfo(getString(R.string.position), songPosn.toString())
            manage.storeInfo(getString(R.string.ID), songPosn.toString())
        })

        viewModel.getSongPosition().observe(this, { integer ->
            songPosn = integer
            if (songPosn >= songList.size) {
                songPosn = 0
            }
            Log.d("Playing", "Position Changed $songPosn")
        })

        lastSongId = manage.get_s_Info(getString(R.string.ID))
        lastShuffle = manage.get_b_Info(getString(R.string.Shuffle))
        lastRepeat = manage.get_b_Info(getString(R.string.Repeat))
        lastRepeatOne = manage.get_b_Info(getString(R.string.RepeatOne))
        playingDuration = manage.get_s_Info(getString(R.string.currentPlayingDuration))

        var start = manage.get_b_Info(getString(R.string.Started))
        val position = manage.get_s_Info(getString(R.string.position))

        if (manage.get_b_Info(getString(R.string.Songs))) {
            songList.clear()
            normalList.clear()
            songList.addAll(viewModel.getTinyDbSongs(getString(R.string.Songs)))
            normalList.addAll(viewModel.getTinyDbSongs(getString(R.string.NormalSongs)))
            Log.d("Playing", songList.size.toString() + "")
            setPlayingList(songList)
            if (position != null) {
                if (songList.size == 0) {
                    start = false
                } else if (songList.size <= position.toInt()) {
                    songPosn = 0
                    setSongPosition(0)
                } else {
                    songPosn = position.toInt()
                    setSongPosition(position.toInt())
                }
            }
        }

        started = start

        if (lastSongId.isNotBlank() || lastSongId.isNotEmpty()) {
            songPosn = lastSongId.toInt()
            if (songList.size != 0) {
                if (songList.size <= songPosn) {
                    setPlayingSong(songList[0])
                } else {
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

        viewModel.getPlaylistSong(getString(R.string.favTracks)).observe(this, { playlists ->
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
        })

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

        loadBanner1()

        CheckUpdate().execute()
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
            if ((requestCode == ALBUM_SONGS
                        || requestCode == ARTIST_SONGS
                        || requestCode == RECENT_ADDED
                        || requestCode == SEARCH_RESULT
                        || requestCode == PLAYLIST)
                && data != null
            ) {
                val position = data.getIntExtra("position", -1)
                val songsArrayList: ArrayList<Song> = data.getParcelableArrayListExtra("songList")!!
                OnFragmentItemClick(position, songsArrayList, false)
            }

            if (requestCode == NOW_PLAYING && data != null) {
                val position = data.getIntExtra("position", -1)
                OnFragmentItemClick(position, songList, true)
            }

            if (!(requestCode == NOW_PLAYING
                        || requestCode == ALBUM_SONGS
                        || requestCode == ARTIST_SONGS
                        || requestCode == RECENT_ADDED
                        || requestCode == SEARCH_RESULT
                        || requestCode == PLAYLIST)
            ) {
                if (deleteUri != null) {
                    val file = File(deleteUri!!.path!!)
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
                    contentResolver.delete(deleteUri!!, null, null)
                    deleteUri = null
                    for (fragment in supportFragmentManager.fragments) {
                        fragment.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }

    override fun OnFragmentItemClick(
        position: Int,
        songsArrayList: ArrayList<Song>,
        nowPlaying: Boolean
    ) {
        songList = songsArrayList.clone() as ArrayList<Song>
        songPosn = position
        fromList = true
        if (!nowPlaying) {
            normalList.clear()
            binding.playingPanel.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            musicSrv!!.shuffle = false
            setRepeatOff()
            manage.storeInfo(getString(R.string.Shuffle), false)
        }
        musicSrv!!.setList(songList)
        setPlayingList(songList)
        musicSrv!!.setSong(songPosn)
        playingDuration = "0"
        musicSrv!!.setPlayingPosition(playingDuration)
        musicSrv!!.playSong()

        if (playbackPaused) {
            playbackPaused = false
        }

        musicBound = true
        played = true
        started = true
        binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
    }

    fun btnPlayPause() {
        if (fromList) {
            if (musicBound) {
                pause()
            } else {
                start()
                played = true
            }
        } else {
            if (musicBound) {
                try {
                    if (!fromButton) {
                        musicSrv!!.playSong()
                        fromButton = true
                    }
                    start()
                    played = true
                } catch (e: Exception) {
                    showSnackBar("Song is deleted or invalid")
                }
            } else {
                pause()
            }
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
                manage.storeInfo(getString(R.string.Shuffle), lastShuffle)
                Log.d(TAG, playingSong!!.title)
                songPosn = songList.indexOf(playingSong)
                setPlayingList(songList)
                musicSrv!!.setSong(songPosn)
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

                manage.storeInfo(getString(R.string.Repeat), lastRepeat)
                manage.storeInfo(getString(R.string.RepeatOne), lastRepeatOne)
            }
            if (v == binding.playingPanel.imgPlaylist) {
                val i = Intent(this@MainActivity, NowPlayingActivity::class.java)
                i.putExtra("songPos", songPosn)
                startActivityForResult(i, NOW_PLAYING)
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

        manage.storeInfo(getString(R.string.Repeat), lastRepeat)
        manage.storeInfo(getString(R.string.RepeatOne), lastRepeatOne)
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
            override fun onPanelSlide(panel: View, slideOffset: Float) {}
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
                        binding.playingPanel.playerController.visibility = View.INVISIBLE

                        if (songList.size > 0) {
                            showSlideImage()
                        } else {
                            hideSlideImage()
                        }
                    } else if (newState == PanelState.EXPANDED) {
                        binding.playingPanel.playerController.visibility = View.VISIBLE
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

        binding.playingPanel.controlBack.load(Uri.parse(playingSong!!.art)) {
            error(R.drawable.logo)
            crossfade(true)
        }

        binding.playingPanel.cardImage.load(Uri.parse(playingSong!!.art)) {
            error(R.drawable.logo)
            transformations(RoundedCornersTransformation(50f))
            crossfade(true)
        }

        binding.playingPanel.slideImage.load(Uri.parse(playingSong!!.art)) {
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
        musicBound = !musicBound
    }

    fun setPauseIcons() {
        binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        musicBound = !musicBound
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
                scan.fetchAllData(contentResolver).observe(this, { aBoolean ->
                    if (aBoolean) {
                        showSnackBar("Media Scan Completed")
                    }
                })
            }
            R.id.share -> try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player")
                var shareMessage = "\nDownload the light weight Playback Music Player app\n\n"
                shareMessage = """
                    ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                    
                    
                    """.trimIndent()
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
        val uri = Uri.parse("market://details?id=$packageName")
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            showSnackBar("Unable to find market app")
        }
    }

    private var alert_seek_max = 5
    private var alert_seek_step = 1
    private var alert_current_value = 0
    private var trackCount = 0

    private lateinit var seekValue: Array<String>

    var timerSelectedValue = true

    private fun showTimer() {
        val timerArray: Array<String> = resources.getStringArray(R.array.timer)
        val trackArray: Array<String> = resources.getStringArray(R.array.tracks)
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val dialogBinding: AlertTimerBinding = AlertTimerBinding.inflate(layoutInflater)
//        val v = LayoutInflater.from(this).inflate(R.layout.alert_timer, null)
        dialog.setView(dialogBinding.root)
//        val dialogBinding.txtTimer = v.findViewById<TextView>(R.id.dialogBinding.txtTimer)
//        val dialogBinding.txtTracks = v.findViewById<TextView>(R.id.dialogBinding.txtTracks)
//        val dialogBinding.txtSeekValue = v.findViewById<TextView>(R.id.dialogBinding.txtSeekValue)
//        val dialogBinding.txtSave = v.findViewById<TextView>(R.id.dialogBinding.txtSave)
//        val dialogBinding.txtOnOff = v.findViewById<TextView>(R.id.dialogBinding.txtOnOff)
//        val dialogBinding.alertSeekBar = v.findViewById<SeekBar>(R.id.dialogBinding.alertSeekBar)
//        val dialogBinding.timerSwitch: SwitchCompat = v.findViewById(R.id.dialogBinding.timerSwitch)
//        val dialogBinding.timerBlocker = v.findViewById<FrameLayout>(R.id.dialogBinding.timerBlocker)
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
        pi = PendingIntent.getBroadcast(this, 1234, i, 0)
        am = getSystemService(ALARM_SERVICE) as AlarmManager
        am!![AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time] = pi
    }

    //play next
    fun playNext() {
        if (songList.size > 0) {
            musicSrv!!.playNext()
            played = true
            if (playbackPaused) {
                playbackPaused = false
            }
            songPosn = musicSrv!!.songPosn
            musicBound = fromList
            fromButton = true
            binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
            binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show()
        }
    }

    //play previous
    fun playPrev() {
        if (songList.size > 0) {
            musicSrv!!.playPrev()
            played = true
            if (playbackPaused) {
                playbackPaused = false
            }
            songPosn = musicSrv!!.songPosn
            musicBound = fromList
            fromButton = true
            binding.playingPanel.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
            binding.playingPanel.btnPlayPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
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
            } else if (musicSrv!! != null && musicSrv!!.isPng) {
                moveTaskToBack(true)
            } else {
                if (doubleBackToExitPressedOnce) {
                    stopApp()
                    return
                }
                doubleBackToExitPressedOnce = true
                showSnackBar("Tap Again to Exit")
                
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
                manage.storeInfo(getString(R.string.ID), songPosn.toString())
                manage.storeInfo(getString(R.string.Started), started)
                manage.storeInfo(getString(R.string.Songs), true)
                manage.storeInfo(getString(R.string.position), songPosn.toString())
                manage.storeInfo(
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
        playbackPaused = true
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

    fun hideHomePanel() {
        binding.container.visibility = View.VISIBLE
        binding.mainLayout.visibility = View.GONE
    }

    private fun showHomePanel() {
        supportFragmentManager.popBackStack()
        binding.container.visibility = View.GONE
        binding.mainLayout.visibility = View.VISIBLE
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
                musicSrv!!.setSong(lastSongId.toInt())
            }
            musicSrv!!.setUIControls(
                binding.playingPanel.seekBar,
                binding.playingPanel.currentPosition,
                binding.playingPanel.totalDuration
            )
            musicBound = true
            loadEqualizerSettings()
            initMediaSessions()
            serviceInitialized = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "onServiceDisconnected: Service Disconnected")
            killApp()
            musicBound = false
            serviceInitialized = false
        }
    }

    //Check of app update

    internal inner class CheckUpdate : CoroutinesAsyncTask<Void?, Void?, Void?>("UpdateCheck") {
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
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            currentVersion = BuildConfig.VERSION_NAME
            if (latestVersion != null) {
                var currentVer = versionStringToLong(currentVersion!!)
                var latestVer = versionStringToLong(latestVersion!!)
                var len = 0
                len =
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
        val split = version.split("\\.")
        val sb = StringBuilder()
        for (s in split) {
            sb.append(s)
        }
        return sb.toString()
    }

    private fun updateAlertDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("New Update Available")
        builder.setMessage("\nCurrent Version: $currentVersion\nLatest Version: $latestVersion\n")
        builder.setCancelable(false)
        builder.setPositiveButton("Update") { dialog, which ->
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Play Store Not Found", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Not Now") { dialog, which -> dialog.dismiss() }
        val ad = builder.create()
        ad.show()
    }

    private fun saveEqualizerSettings() {
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
    }

    private fun loadEqualizerSettings() {
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
    }

    var observer: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val provider = SongsProvider()
            provider.fetchAllData(contentResolver).observe(this@MainActivity, { aBoolean ->
                if (aBoolean) {
                    checkInPlaylists()
                }
            })
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
        val songListByName = songListByName.value!!
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
                    val manage = PrefManager(this)
                    manage.storeInfo(getString(R.string.Songs), false)
                }
                tinydb.putListObject(playListName, songList)
            }
            val playlists = repository.allPlaylistSongs
            for ((_, _, s) in playlists) {
                if (!songListByName.contains(s)) {
                    repository.removeSong(s.id)
                }
            }
            if (getPlayingListData().value != null) {
                val songsToRemove = ArrayList<Song>()
                for (s in getPlayingListData().value!!) {
                    if (!songListByName.contains(s)) {
                        songsToRemove.add(s)
                    }
                }
                deletionProcess = true
                for (s in songsToRemove) {
                    removeFromPlayingList(s)
                }
            }
            val lastPlayedRepository = LastPlayedRepository(this)
            lastPlayedRepository.getLastPlayedSongsList()
                .observe(this, { lastPlayed: List<LastPlayed>? ->
                    if (lastPlayed != null) {
                        for ((s) in lastPlayed) {
                            if (!songListByName.contains(s)) {
                                lastPlayedRepository.deleteSongFromLastPlayed(s.id)
                            }
                        }
                    }
                })
            val mostPlayedRepository = MostPlayedRepository(this)
            mostPlayedRepository.getMostPlayedSongs()
                .observe(this, { mostPlayed: List<MostPlayed>? ->
                    if (mostPlayed != null) {
                        for ((s) in mostPlayed) {
                            if (!songListByName.contains(s)) {
                                mostPlayedRepository.deleteMostPlayedSong(s.id)
                            }
                        }
                    }
                })
        } else {
            started = false
            stopApp()
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.slidingLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREF_KEY = "equalizer"
        var d = 0
        const val ALBUM_SONGS = 1
        const val ARTIST_SONGS = 2
        const val NOW_PLAYING = 3
        const val SEARCH_RESULT = 4
        const val PLAYLIST = 5
        const val RECENT_ADDED = 6

        var instance: MainActivity? = null
            private set
    }
}