package com.ravisharma.playbackmusic.activities

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.databinding.ActivitySingleSongPlayBinding
import com.ravisharma.playbackmusic.utils.CoroutinesAsyncTask
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class SingleSongPlayActivity : AppCompatActivity(), OnPreparedListener, OnCompletionListener,
    OnAudioFocusChangeListener {

    private var mProgressRunner: Runnable? = null
    private var player: MediaPlayer? = null
    private var audioManager: AudioManager? = null

    private var numActivity = 0
    private var played = false

    private lateinit var binding: ActivitySingleSongPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleSongPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setFinishOnTouchOutside(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        } else {
            runTask()
        }
    }

    private fun runTask() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        loadBanner()

        val data = intent
        setPlayerUI(data.data)
        binding.btnPlayPause.setOnClickListener(View.OnClickListener {
            if (player == null) {
                setMediaPlayer(data.data)
            } else {
                if (player!!.isPlaying) {
                    binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp)
                    player!!.pause()
                } else {
                    requestAudioFocus()
                    binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
                    player!!.start()
                }
            }
        })
        mProgressRunner = Runnable {
            try {
                if (binding.seekBar != null) {
                    binding.seekBar.progress = player!!.currentPosition
                    binding.seekBar.postDelayed(mProgressRunner, 100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val taskList = manager.getRunningTasks(10)
        numActivity = taskList[0].numActivities
    }

    private fun loadBanner() {
        val adView = AdView(this)
        adView.adUnitId = getString(R.string.SingleSongActId)

        binding.bannerContainerSingleSong.addView(adView)

        val adRequest = AdRequest.Builder().build()
        val adSize = AdSize.BANNER
        adView.setAdSize(adSize)
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.bannerContainerSingleSong.visibility = View.VISIBLE
            }

            override fun onAdClosed() {
                super.onAdClosed()
                binding.bannerContainerSingleSong.visibility = View.GONE
            }
        }
    }

    private fun setMediaPlayer(songUri: Uri?) {
        player = MediaPlayer()
        player!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player!!.setOnPreparedListener(this)
        player!!.setOnCompletionListener(this)
        try {
            player!!.setDataSource(applicationContext, songUri!!)
        } catch (ignored: Exception) {
        }
        try {
            player!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && player != null) {
                    player!!.seekTo(progress)
                }
                binding.tvCurrentPosition.text = String.format(
                    "%d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(progress.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(progress.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.toLong()))
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun requestAudioFocus(): Boolean {
        val result = audioManager!!.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            mProgressRunner!!.run()
            return true
        }
        //Could not gain focus
        return false
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!.abandonAudioFocus(this)
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (mp.currentPosition > 0) {
            mp.reset()
            binding.seekBar.progress = 0
            binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp)
            player = null
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        played = true
        requestAudioFocus()
        mp.start()
        binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
        binding.seekBar.max = mp.duration
        binding.seekBar.postDelayed(mProgressRunner, 100)
        binding.tvTotalDuration.text = String.format(
            "%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(player!!.duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(player!!.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            player!!.duration.toLong()
                        )
                    )
        )
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!player!!.isPlaying) {
                    player!!.start()
                    binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp)
                }
                player!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (player!!.isPlaying) {
                    player!!.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp)
                }
                killApp()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (player!!.isPlaying) {
                    player!!.setVolume(0.1f, 0.1f)
                    player!!.pause()
                }
                killApp()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (player!!.isPlaying) {
                player!!.setVolume(0.1f, 0.1f)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (played) {
            killApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        killApp()
    }

    private fun killApp() {
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
        if (binding.seekBar != null) {
            binding.seekBar.removeCallbacks(mProgressRunner)
        }
        if (mProgressRunner != null) {
            mProgressRunner = null
        }
        if (audioManager != null) {
            removeAudioFocus()
            audioManager = null
        }
        if (numActivity > 1) {
            finish()
        } else {
            finishAndRemoveTask()
            System.exit(0)
        }
    }

    private fun setPlayerUI(uri: Uri?) {
        Log.d("SongURI", uri.toString() + "")
        FetchSongInfo(contentResolver).execute(uri)
    }

    inner class MediaFile {
        var trackUri: Uri? = null
        var thisTitle: String? = null
        var thisArtist: String? = null
        var albumArt: Uri? = null
    }

    inner class FetchSongInfo(var resolver: ContentResolver) :
        CoroutinesAsyncTask<Uri?, Void?, MediaFile?>("SingleSong") {
        private fun getSong(selection: String, args: Array<String>): MediaFile? {
            val mediaFile = MediaFile()
            val musicResolver = resolver
            val musicCursor = musicResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                selection, args, null
            )
            Log.d("Song", selection + " " + args[0])
            try {
                if (musicCursor != null /*&& musicCursor.getCount() > 0*/ && musicCursor.moveToFirst()) {
                    Log.d("Song", musicCursor.count.toString() + "")
                    val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val thisId = musicCursor.getLong(idColumn)
                    val thisTitle = musicCursor.getString(titleColumn)
                    val thisArtist = musicCursor.getString(artistColumn)
                    val thisAlbumAid = musicCursor.getLong(albumIdColumn)
                    val ART_CONTENT = Uri.parse("content://media/external/audio/albumart")
                    val albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid)
                    val trackUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        thisId
                    )
                    mediaFile.albumArt = albumArt
                    mediaFile.thisArtist = thisArtist
                    mediaFile.thisTitle = thisTitle
                    mediaFile.trackUri = trackUri
                    musicCursor.close()
                } else {
                    return null
                }
            } catch (e: Exception) {
                Log.d("Error", e.toString())
                return null
            }
            return mediaFile
        }

        private fun getFilePathFromUri(context: Context, uri: Uri): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri, projection, null, null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } catch (e: Exception) {
                Log.e("Error", e.message!!)
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun getSongIdFromMediaProvider(uri: Uri): String {
            return DocumentsContract.getDocumentId(uri).split(":").toTypedArray()[1]
        }

        override fun doInBackground(vararg uris: Uri?): MediaFile? {
            var song: MediaFile? = null
            val uri = uris[0]!!
            if (uri.scheme != null && uri.authority != null) {
                Log.d("SingleScreen", "1")
                if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                    var songId: String? = null
                    if (uri.authority == "com.android.providers.media.documents") {
                        songId = getSongIdFromMediaProvider(uri)
                        Log.d("SingleScreen", "2")
                    } else if (uri.authority == "media") {
                        songId = uri.lastPathSegment
                        Log.d("SingleScreen", "3")
                    }
                    if (songId != null) {
                        song = getSong(MediaStore.Audio.AudioColumns._ID + "=?", arrayOf(songId))
                        Log.d("SingleScreen", "4")
                    }
                }
            }
            if (song == null) {
                Log.d("SingleScreen", "5")
                var songFile: File? = null
                if (uri.authority != null && uri.authority == "com.android.externalstorage.documents") {
                    Log.d("SingleScreen", "6")
                    songFile = File(
                        Environment.getExternalStorageDirectory(),
                        Pattern.compile(":").split(uri.path!!, 2)[1]
                    )
                }
                if (songFile == null) {
                    Log.d("SingleScreen", "7")
                    val path = getFilePathFromUri(this@SingleSongPlayActivity, uri)
                    if (path != null) songFile = File(path)
                }
                if (songFile == null && uri.path != null) {
                    Log.d("SingleScreen", "8")
                    songFile = File(uri.path)
                }
                if (songFile != null) {
                    Log.d("SingleScreen", "9")
                    Log.d("Song", songFile.absolutePath + "")
                    song = getSong(
                        MediaStore.Audio.AudioColumns.DATA + "=?",
                        arrayOf(songFile.absolutePath)
                    )
                }
            }
            return song
        }

        override fun onPostExecute(result: MediaFile?) {
            super.onPostExecute(result)
            if (result != null) {
                binding.apply {
                    tvSongTitle.text = result.thisTitle
                    tvSongArtist.text = result.thisArtist
                    ivSongThumb.load(result.albumArt) {
                        error(R.drawable.logo)
                        crossfade(true)
                    }
                    setMediaPlayer(result.trackUri)
                    spinKit.visibility = View.GONE
                    infoLayout.visibility = View.VISIBLE
                }
            } else {
                killApp()
                Toast.makeText(
                    this@SingleSongPlayActivity,
                    "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions33 = listOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
    )

    private val permissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
    )

    /*
     *  Permissions Checking
     * */
    private fun checkPermission() {
        var isPermissionGranted = false
        val permissionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions33
        } else {
            permissions
        }
        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isPermissionGranted = false
                break
            }
            isPermissionGranted = true
        }
        if(isPermissionGranted) {
            runTask()
        } else {
            ActivityCompat.requestPermissions(
                this, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions33.toTypedArray()
                } else {
                    permissions.toTypedArray()
                }, 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runTask()
            } else {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission
                    )
                ) {
                    finish()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(getString(R.string.permissionAlert))
                        .setPositiveButton(getString(R.string.Grant)) { dialog, id ->
                            finish()
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts(getString(R.string.packageName), packageName, null)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(R.string.dont)) { dialog, id -> finish() }
                    builder.setCancelable(false)
                    builder.create().show()
                }
            }
        }
    }
}