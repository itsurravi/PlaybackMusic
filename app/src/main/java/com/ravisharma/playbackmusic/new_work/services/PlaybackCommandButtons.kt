package com.ravisharma.playbackmusic.new_work.services

import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import com.ravisharma.playbackmusic.R

object PlaybackCommandButtons {

    val liked by lazy {
        CommandButton.Builder()
            .apply {
                setSessionCommand(SessionCommand(PlaybackCommands.UNLIKE, Bundle()))
                setDisplayName("Unlike")
                setIconResId(R.drawable.ic_baseline_favorite_24)
            }.build()
    }

    val unliked by lazy {
        CommandButton.Builder()
            .apply {
                setSessionCommand(SessionCommand(PlaybackCommands.LIKE, Bundle()))
                setDisplayName("Like")
                setIconResId(R.drawable.ic_baseline_favorite_border_24)
            }.build()
    }

    val previous by lazy {
        CommandButton.Builder()
            .apply {
                setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                setDisplayName("Previous")
                setIconResId(R.drawable.ic_baseline_skip_previous_40)
            }.build()
    }


    val playPause by lazy {
        CommandButton.Builder()
            .apply {
                setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                setDisplayName("PlayPause")
                setIconResId(R.drawable.ic_baseline_skip_previous_40)
            }.build()
    }

    val next by lazy {
        CommandButton.Builder()
            .apply {
                setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                setDisplayName("Next")
                setIconResId(R.drawable.ic_baseline_skip_next_40)
            }.build()
    }

    val cancel by lazy {
        CommandButton.Builder()
            .apply {
                setSessionCommand(SessionCommand(PlaybackCommands.CLOSE, Bundle()))
                setDisplayName("Close")
                setIconResId(R.drawable.ic_baseline_close_40)
            }.build()
    }
}