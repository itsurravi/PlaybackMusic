package com.ravisharma.playbackmusic.new_work.services.data

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.new_work.services.PlaybackService
import com.ravisharma.playbackmusic.new_work.services.toMediaItem
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicLong

interface PlayerService {
    suspend fun startServiceIfNotRunning(songs: List<Song>, startPlayingFromPosition: Int)
    fun isServiceRunning(): Boolean
}

class PlayerServiceImpl(
    private val context: Context,
    private val queueService: QueueService,
) : PlayerService {

    private val lastCallTime = AtomicLong(0)

    @UnstableApi
    override fun isServiceRunning(): Boolean {
        return PlaybackService.isRunning.get()
    }

    @SuppressLint("RestrictedApi")
    @UnstableApi
    override suspend fun startServiceIfNotRunning(
        songs: List<Song>,
        startPlayingFromPosition: Int
    ) {
        synchronized(lastCallTime) {
            if (lastCallTime.get() + 1000 >= System.currentTimeMillis()) return
            lastCallTime.set(System.currentTimeMillis())
        }

        queueService.setQueue(songs, startPlayingFromPosition)
        if (isServiceRunning()) return

        val factory = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()

        val repeatMode = queueService.repeatMode.first().toExoPlayerRepeatMode()

        factory.addListener({
            val mediaController = factory.let {
                if (it.isDone)
                    it.get()
                else
                    null
            }

            mediaController?.let {
                it.stop()
                it.clearMediaItems()
                it.addMediaItems(songs.map(Song::toMediaItem))
                it.prepare()
                it.seekTo(startPlayingFromPosition, 0)
                it.repeatMode = repeatMode
                /*playbackParameters = preferenceProvider.playbackParams.value
                .toCorrectedParams()
                .toExoPlayerPlaybackParameters()*/
                it.play()
            }
        }, MoreExecutors.directExecutor())
    }
}