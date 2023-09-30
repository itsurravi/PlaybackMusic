package com.ravisharma.playbackmusic.new_work.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.new_work.Constants
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.ui.activity.NewPlayerActivity

class PlaybackNotificationManager(
    val context: Context
) {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
    }

    companion object {
        const val PLAYER_SERVICE = "MusicService"
        const val PLAYER_NOTIFICATION_ID = 12
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val requiredChannels = listOf(
            NotificationChannel(
                PLAYER_SERVICE,
                "Player",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(requiredChannels)
    }


    private val outlinedLikeAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_favorite_not_24),
        "Like",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.LIKE_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_LIKE
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val filledLikedAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_favorite_24),
        "Unlike",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.LIKE_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_LIKE
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val previousAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_previous_24),
        "Previous",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.PREVIOUS_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_PREVIOUS
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val nextAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_next_24),
        "Next",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.NEXT_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_NEXT
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val pauseAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_pause_24),
        "Pause",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.PAUSE_PLAY_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_PAUSE_PLAY
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val playAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_play_24),
        "Play",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.PAUSE_PLAY_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_PAUSE_PLAY
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val cancelAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, R.drawable.ic_close_24),
        "Close",
        PendingIntent.getBroadcast(
            context, PlaybackBroadcastReceiver.CANCEL_ACTION_REQUEST_CODE,
            Intent(Constants.PACKAGE_NAME).putExtra(
                PlaybackBroadcastReceiver.AUDIO_CONTROL,
                PlaybackBroadcastReceiver.PLAYER_CANCEL
            ),
            PendingIntent.FLAG_IMMUTABLE
        )
    ).build()

    private val activityIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, NewPlayerActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )

    fun getPlayerNotification(
        session: MediaSessionCompat,
        showPlayButton: Boolean,
        isLiked: Boolean,
        artBitmap: Bitmap,
    ): Notification {
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(1, 2, 3)
            .setMediaSession(session.sessionToken)
        return NotificationCompat.Builder(context, PLAYER_SERVICE).apply {
            setSmallIcon(R.drawable.ic_music_note)
            setContentTitle("Now Playing")
            setContentText("")
            setOngoing(true)
            setLargeIcon(artBitmap)
            priority = NotificationCompat.PRIORITY_MAX
            setSilent(true)
            setStyle(mediaStyle)
            addAction(if (isLiked) filledLikedAction else outlinedLikeAction)
            addAction(previousAction)
            addAction(if (showPlayButton) playAction else pauseAction)
            addAction(nextAction)
            addAction(cancelAction)
            setContentIntent(activityIntent)  // use with android:launchMode="singleTask" in manifest
        }.build()
    }
}