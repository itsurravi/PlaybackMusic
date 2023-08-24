package com.ravisharma.playbackmusic.new_work.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.notification.PlaybackNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : Service(), DataManager.Callback, PlaybackBroadcastReceiver.Callback {

    @Inject
    lateinit var notificationManager: PlaybackNotificationManager

    @Inject
    lateinit var dataManager: DataManager

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private var broadcastReceiver: PlaybackBroadcastReceiver? = null

    private var systemNotificationManager: NotificationManager? = null

    private val job = SupervisorJob()

    private val scope = CoroutineScope(job + Dispatchers.Default)

    companion object {
        const val MEDIA_SESSION = "media_session"
    }

    private lateinit var mediaSession: MediaSessionCompat

    override fun onBind(intent: Intent?): IBinder? = null

    private val exoPlayerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            try {
                if (mediaItem != null) {
                    dataManager.updateCurrentSong(exoPlayer.currentMediaItemIndex)
                }
            } catch (e: Exception) {
                Log.e("exoPlayerListener", "${e.message}")
            }
            updateMediaSessionState()
            updateMediaSessionMetadata()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            updateMediaSessionState()
            updateMediaSessionMetadata()
        }
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            onBroadcastPausePlay()
        }

        override fun onPause() {
            super.onPause()
            onBroadcastPausePlay()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            onBroadcastNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            onBroadcastPrevious()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            exoPlayer.seekTo(pos)
            updateMediaSessionState()
            updateMediaSessionMetadata()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        broadcastReceiver = PlaybackBroadcastReceiver()
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION)
        systemNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        dataManager.setPlayerRunning(this)
        IntentFilter(Constants.PACKAGE_NAME).also {
            registerReceiver(broadcastReceiver, it)
        }

        broadcastReceiver?.startListening(this)
        mediaSession.setCallback(mediaSessionCallback)
        exoPlayer.addListener(exoPlayerListener)

        startForeground(
            PlaybackNotificationManager.PLAYER_NOTIFICATION_ID,
            notificationManager.getPlayerNotification(
                session = mediaSession,
                showPlayButton = false,
                isLiked = dataManager.getSongAtIndex(exoPlayer.currentMediaItemIndex)?.favourite
                    ?: false
            )
        )

        scope.launch {
            dataManager.repeatMode.collect {
                withContext(Dispatchers.Main) { exoPlayer.repeatMode = it.toExoPlayerRepeatMode() }
            }
        }

//        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.isSpeakerphoneOn = true

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun stopService() {
        unregisterReceiver(broadcastReceiver)

        val index = exoPlayer.currentMediaItemIndex

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.removeListener(exoPlayerListener)

        mediaSession.release()

        dataManager.stopPlayerRunning(index)
        broadcastReceiver?.stopListening()
        systemNotificationManager?.cancel(PlaybackNotificationManager.PLAYER_NOTIFICATION_ID)

        scope.cancel()
        job.cancel()

        systemNotificationManager = null
        broadcastReceiver = null
    }

    private fun updateMediaSessionState() {
        scope.launch {
            delay(100)
            withContext(Dispatchers.Main) {
                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder().apply {
                        setState(
                            if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                            exoPlayer.currentPosition,
                            1f,
                        )
                        setActions(
                            (if (exoPlayer.isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY)
                                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or PlaybackStateCompat.ACTION_SEEK_TO
                        )
                    }.build()
                )
            }
        }
    }

    private fun updateMediaSessionMetadata() {
        scope.launch {
            var currentSong: Song? = null
            withContext(Dispatchers.Main) {
                currentSong = dataManager.getSongAtIndex(exoPlayer.currentMediaItemIndex)
            }
            if (currentSong == null) return@launch
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putString(
                        MediaMetadataCompat.METADATA_KEY_TITLE,
                        currentSong!!.title
                    )
                    putString(
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        currentSong!!.artist
                    )
                    putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        currentSong!!.artUri
                    )
                    putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        currentSong!!.durationMillis
                    )
                }.build()
            )
            delay(100)
            withContext(Dispatchers.Main) {
                systemNotificationManager?.notify(
                    PlaybackNotificationManager.PLAYER_NOTIFICATION_ID,
                    notificationManager.getPlayerNotification(
                        session = mediaSession,
                        showPlayButton = !exoPlayer.isPlaying,
                        isLiked = dataManager.getSongAtIndex(exoPlayer.currentMediaItemIndex)?.favourite
                            ?: false
                    )
                )
            }
        }
    }

    @Synchronized
    override fun setQueue(newQueue: List<Song>, startPlayingFromIndex: Int) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        val mediaItems = newQueue.map {
            MediaItem.fromUri(it.location)
        }
        exoPlayer.addMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.seekTo(startPlayingFromIndex, 0)
        exoPlayer.play()
        updateMediaSessionState()
        updateMediaSessionMetadata()
    }

    @Synchronized
    override fun updateExoList(shuffled: Boolean, list: List<Song>) {
        if (shuffled) {
            val count = exoPlayer.mediaItemCount
            for (i in count downTo 0) {
                if (i != exoPlayer.currentMediaItemIndex) {
                    exoPlayer.removeMediaItem(i)
                }
            }
            val mediaItems = list.map {
                MediaItem.fromUri(it.location)
            }
            exoPlayer.addMediaItems(mediaItems)
        } else {
            val count = exoPlayer.mediaItemCount
            val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri

            val index = list.indexOfFirst { it.location == currentUri.toString() }

            val mList = list.subList(0, index)
            val nList = list.subList(index, list.size)

            for (i in count downTo 0) {
                if (i != exoPlayer.currentMediaItemIndex) {
                    exoPlayer.removeMediaItem(i)
                }
            }

            if(mList.isNotEmpty()) {
                mList.forEachIndexed { index, song ->
                    exoPlayer.addMediaItem(index, MediaItem.fromUri(song.location))
                }
            }

            if(nList.isNotEmpty() && nList.size > 1) {
                exoPlayer.addMediaItems(nList.subList(1, nList.size).map {
                    MediaItem.fromUri(it.location)
                })
            }
        }
    }

    @Synchronized
    override fun addToQueue(song: Song) {
        exoPlayer.addMediaItem(MediaItem.fromUri(song.location))
    }

    @Synchronized
    override fun addNextInQueue(song: Song): Int {
        val index = exoPlayer.currentMediaItemIndex + 1
        exoPlayer.addMediaItem(index, MediaItem.fromUri(song.location))
        return index
    }

    @Synchronized
    override fun updateNotification() {
        updateMediaSessionState()
        updateMediaSessionMetadata()
    }

    /**
     * Called when user clicks play/pause button in notification.
     * Player.Listener onIsPlayingChanged gets called.
     */
    override fun onBroadcastPausePlay() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (!exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekTo(0, 0)
            }
            exoPlayer.play()
        }
    }

    /**
     * Called when user clicks next button in notification.
     * If we have next song in queue we skip to it.
     * Player.Listener onMediaItemTransition gets called.
     */
    override fun onBroadcastNext() {
        if (!exoPlayer.hasNextMediaItem()) {
            showToast("No next song in queue")
            return
        }
        exoPlayer.seekToNextMediaItem()
    }

    /**
     * Called when user clicks previous button in notification.
     * If we have previous song in queue we skip to it.
     * Player.Listener onMediaItemTransition gets called.
     */
    override fun onBroadcastPrevious() {
        if (!exoPlayer.hasPreviousMediaItem()) {
            showToast("No previous song in queue")
            return
        }
        exoPlayer.seekToPreviousMediaItem()
    }

    /**
     * Called when user clicks on like icon (filled and outlined both)
     * This fetches the current song, toggles the favourite and passes the updated song to DataManager
     * DataManager then calls updateNotification of DataManager.Callback
     */
    override fun onBroadcastLike() {
        val currentSong = dataManager.getSongAtIndex(exoPlayer.currentMediaItemIndex) ?: return
        val updatedSong = currentSong.copy(favourite = !currentSong.favourite)
        scope.launch {
            dataManager.updateSong(updatedSong)
        }
    }

    /**
     * Called when user clicks close button in notification
     * This stops the service and onDestroy is called
     */
    override fun onBroadcastCancel() {
        // Deprecated in api level 33
        stopForeground(true)
        stopSelf()
    }
}