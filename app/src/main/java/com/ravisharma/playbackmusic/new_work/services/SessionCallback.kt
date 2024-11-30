package com.ravisharma.playbackmusic.new_work.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.SongService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import com.ravisharma.playbackmusic.new_work.utils.Constants
import com.ravisharma.playbackmusic.new_work.data_proto.QueueState
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@ServiceScoped
class SessionCallback @Inject constructor(
    @ApplicationContext context: Context,
    private val queueService: QueueService,
    private val songService: SongService,
    private val scope: CoroutineScope,
    private val queueState: DataStore<QueueState>,
) : MediaSession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableCommands = connectionResult.availableSessionCommands.buildUpon()
        availableCommands.add(PlaybackCommandButtons.liked.sessionCommand!!)
        availableCommands.add(PlaybackCommandButtons.unliked.sessionCommand!!)
        availableCommands.add(PlaybackCommandButtons.cancel.sessionCommand!!)
        return MediaSession.ConnectionResult.accept(
            availableCommands.build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        super.onPostConnect(session, controller)
        val isLiked = queueService.currentSong.value?.favourite ?: false
        session.setCustomLayout(
            controller,
            listOf(
                if (isLiked) PlaybackCommandButtons.liked else PlaybackCommandButtons.unliked,
                PlaybackCommandButtons.previous,
                PlaybackCommandButtons.playPause,
                PlaybackCommandButtons.next,
                PlaybackCommandButtons.cancel
            )
        )
    }

    private val closeAction = PendingIntent.getBroadcast(
        context, PlaybackBroadcastReceiver.CANCEL_ACTION_REQUEST_CODE,
        Intent(Constants.PACKAGE_NAME).putExtra(
            PlaybackBroadcastReceiver.AUDIO_CONTROL,
            PlaybackBroadcastReceiver.PLAYER_CANCEL
        ),
        PendingIntent.FLAG_IMMUTABLE
    )

    private val likeUnlikeAction = PendingIntent.getBroadcast(
        context, PlaybackBroadcastReceiver.LIKE_ACTION_REQUEST_CODE,
        Intent(Constants.PACKAGE_NAME).putExtra(
            PlaybackBroadcastReceiver.AUDIO_CONTROL,
            PlaybackBroadcastReceiver.PLAYER_LIKE
        ),
        PendingIntent.FLAG_IMMUTABLE
    )

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        val result = SettableFuture.create<SessionResult>()
        when (customCommand.customAction) {
            PlaybackCommands.LIKE, PlaybackCommands.UNLIKE -> {
                likeUnlikeAction.send()
            }

            PlaybackCommands.CLOSE -> {
                closeAction.send()
            }
        }
        result.set(SessionResult(SessionResult.RESULT_SUCCESS))
        return result
    }

    @UnstableApi
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val result = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
        scope.launch {
            val state = queueState.data.first()
            val songs = songService.getSongsFromLocations(state.locationsList)
            val locationMap = buildMap {
                for (song in songs) {
                    put(song.location, song)
                }
            }
            val orderedSongs = buildList {
                for (location in state.locationsList) {
                    if (locationMap.containsKey(location)) {
                        add(locationMap[location]!!)
                    }
                }
            }
            queueService.clearQueue()
            queueService.setQueue(orderedSongs, state.startIndex)
            result.set(
                MediaSession.MediaItemsWithStartPosition(
                    orderedSongs.map(Song::toMediaItem),
                    state.startIndex,
                    state.startPositionMs
                )
            )
        }
        return result
    }

}