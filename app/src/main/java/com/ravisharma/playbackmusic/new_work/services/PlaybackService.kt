package com.ravisharma.playbackmusic.new_work.services

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.SleepTimerService
import com.ravisharma.playbackmusic.new_work.services.data.SongService
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.data.provider.SongExtractor
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.data_proto.QueueStateProvider
import com.ravisharma.playbackmusic.new_work.notification.PlaybackNotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.system.exitProcess

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService(), QueueService.Listener, PlaybackBroadcastReceiver.Callback {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    @Inject lateinit var songExtractor: SongExtractor
    @Inject lateinit var queueService: QueueService
    @Inject lateinit var songService: SongService
    @Inject lateinit var sleepTimerService: SleepTimerService
    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var queueStateProvider: QueueStateProvider
    @Inject lateinit var sessionCallback: SessionCallback
    @Inject lateinit var notificationProvider: PlaybackNotificationProvider

    private var broadcastReceiver: PlaybackBroadcastReceiver? = null

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)

    companion object {
        const val MEDIA_SESSION = "media_session"
        val isRunning = AtomicBoolean(false)
    }

    private var mediaSession: MediaSession? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        isRunning.set(true)
        broadcastReceiver = PlaybackBroadcastReceiver()
        mediaSession = MediaSession.Builder(applicationContext, exoPlayer)
            .setCallback(sessionCallback)
            .setId(System.currentTimeMillis().toString())
            .build()

        queueService.addListener(this)

        IntentFilter(Constants.PACKAGE_NAME).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiver, it, RECEIVER_EXPORTED)
            } else {
                registerReceiver(broadcastReceiver, it)
            }
        }
        broadcastReceiver?.startListening(this)
        exoPlayer.addListener(exoPlayerListener)

        /*scope.launch {
            preferencesProvider.playbackParams.collect {
                val params = it.toCorrectedParams().toExoPlayerPlaybackParameters()
                withContext(Dispatchers.Main) {
                    exoPlayer.playbackParameters = params
                }
            }
        }*/
        scope.launch {
            queueService.repeatMode.collect {
                withContext(Dispatchers.Main) { exoPlayer.repeatMode = it.toExoPlayerRepeatMode() }
            }
        }

        setMediaNotificationProvider(notificationProvider)
    }

    private val exoPlayerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            try {
                queueService.setCurrentSong(exoPlayer.currentMediaItemIndex)
                queueService.getSongAtIndex(exoPlayer.currentMediaItemIndex)?.let { song ->
                    updateNotification(song.favourite)
                    /*val broadcast = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        putExtra(WidgetBroadcast.WIDGET_BROADCAST, WidgetBroadcast.SONG_CHANGED)
                        putExtra("imageUri", song.artUri)
                        putExtra("title", song.title)
                        putExtra("artist", song.artist)
                        putExtra("album", song.album)
                    }
                    this@ZenPlayer.applicationContext.sendBroadcast(broadcast)*/
                }
            } catch (_: Exception) {
                
            }
        }

        /*override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            val broadcast = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                putExtra(WidgetBroadcast.WIDGET_BROADCAST, WidgetBroadcast.IS_PLAYING_CHANGED)
                putExtra("isPlaying", isPlaying)
            }
            this@ZenPlayer.applicationContext.sendBroadcast(broadcast)
        }*/

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                songExtractor.cleanData()
                onBroadcastCancel()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler(Looper.getMainLooper()).postDelayed({ exitProcess(0) }, 1500)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun stopService() {
        isRunning.set(false)
        if(queueService.queue.isNotEmpty()) {
            queueStateProvider.saveState(
                queue = queueService.queue.map { it.location },
                startIndex = exoPlayer.currentMediaItemIndex,
                startPosition = exoPlayer.currentPosition
            )
        }
        with(queueService) {
            clearQueue()
            removeListener(this@PlaybackService)
        }
        with(exoPlayer) {
            stop()
            clearMediaItems()
            removeListener(exoPlayerListener)
        }
        scope.cancel()
        job.cancel()

        sleepTimerService.cancel()

        broadcastReceiver?.let { unregisterReceiver(it) }
        broadcastReceiver?.stopListening()
        broadcastReceiver = null
    }

    private fun updateNotification(isLiked: Boolean) {
        mediaSession?.setCustomLayout(
            listOf(
                if (isLiked) PlaybackCommandButtons.liked else PlaybackCommandButtons.unliked,
                PlaybackCommandButtons.previous,
                PlaybackCommandButtons.playPause,
                PlaybackCommandButtons.next,
                PlaybackCommandButtons.cancel
            )
        )
    }

    private fun setQueue(mediaItems: List<MediaItem>, startPosition: Int) {
        scope.launch {
            val repeatMode = queueService.repeatMode.first()
            withContext(Dispatchers.Main) {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.prepare()
                exoPlayer.seekTo(startPosition, 0)
                exoPlayer.repeatMode = repeatMode.toExoPlayerRepeatMode()
                /*exoPlayer.playbackParameters = preferencesProvider.playbackParams.value
                    .toCorrectedParams()
                    .toExoPlayerPlaybackParameters()*/
                exoPlayer.play()
            }
        }
    }

    override fun onAppend(song: Song) {
        exoPlayer.addMediaItem(song.toMediaItem())
    }

    override fun onAppend(songs: List<Song>) {
        exoPlayer.addMediaItems(
            songs.map(Song::toMediaItem)
        )
    }

    override fun onUpdate(updatedSong: Song, position: Int) {
        scope.launch {
            val performUpdate = withContext(Dispatchers.Main) {
                exoPlayer.currentMediaItemIndex == position
            }
            if (!performUpdate) return@launch
            withContext(Dispatchers.Main) {
                updateNotification(updatedSong.favourite)
            }
        }
    }

    override fun onMove(from: Int, to: Int) {
        exoPlayer.moveMediaItem(from, to)
    }

    override fun onClear() {}

    override fun onSetQueue(songs: List<Song>, startPlayingFromPosition: Int) {
        val mediaItems = songs.map(Song::toMediaItem)
        setQueue(mediaItems, startPlayingFromPosition)
    }

    /**
     * Called when user clicks play/pause button in notification.
     * Player.Listener onIsPlayingChanged gets called.
     */
    override fun onBroadcastPausePlay() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
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
        val currentSong = queueService.getSongAtIndex(exoPlayer.currentMediaItemIndex) ?: return
        val updatedSong = currentSong.copy(favourite = !currentSong.favourite)
        scope.launch {
            queueService.update(updatedSong)
            songService.updateSong(updatedSong)
        }
    }

    /**
     * Called when user clicks close button in notification
     * This stops the service and onDestroy is called
     */
    override fun onBroadcastCancel() {
        /**
         * To close the media session, first call mediaSession.release followed by stopSelf()
         * See issue: https://github.com/androidx/media/issues/389#issuecomment-1546611545
         */
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        stopService()
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
        onBroadcastCancel()
    }

}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder().apply {
        setUri(Uri.fromFile(File(this@toMediaItem.location)))
        setMediaId(this@toMediaItem.location)
        setMediaMetadata(
            MediaMetadata.Builder().apply {
                setArtworkUri(this@toMediaItem.artUri?.toUri())
                setTitle(this@toMediaItem.title)
                setArtist(this@toMediaItem.artist)
                setIsBrowsable(false)
                setIsPlayable(true)
            }.build()
        )
    }.build()
}