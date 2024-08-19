package com.ravisharma.playbackmusic.new_work.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ravisharma.playbackmusic.new_work.utils.Constants

class PlaybackBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val AUDIO_CONTROL = "audio_control"
        const val PLAYER_PAUSE_PLAY = Constants.PACKAGE_NAME + ".ACTION_PAUSE"
        const val PLAYER_NEXT = Constants.PACKAGE_NAME + ".ACTION_NEXT"
        const val PLAYER_PREVIOUS = Constants.PACKAGE_NAME + ".ACTION_PREVIOUS"
        const val PLAYER_CANCEL = Constants.PACKAGE_NAME + ".ACTION_CANCEL"
        const val PLAYER_LIKE = Constants.PACKAGE_NAME + ".ACTION_LIKE"
        const val PAUSE_PLAY_ACTION_REQUEST_CODE = 1001
        const val NEXT_ACTION_REQUEST_CODE = 1002
        const val PREVIOUS_ACTION_REQUEST_CODE = 1003
        const val CANCEL_ACTION_REQUEST_CODE = 1004
        const val LIKE_ACTION_REQUEST_CODE = 1005
    }

    private var callback: Callback? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.extras?.getString(AUDIO_CONTROL) ?: return
        when (action) {
            PLAYER_NEXT -> callback?.onBroadcastNext()
            PLAYER_PAUSE_PLAY -> callback?.onBroadcastPausePlay()
            PLAYER_PREVIOUS -> callback?.onBroadcastPrevious()
            PLAYER_LIKE -> callback?.onBroadcastLike()
            PLAYER_CANCEL -> callback?.onBroadcastCancel()
            else -> {
                Log.d("no action matched", "no action matched -> $action")
            }
        }
    }

    fun startListening(callback: Callback) {
        this.callback = callback
    }

    fun stopListening() {
        this.callback = null
    }

    interface Callback {
        fun onBroadcastPausePlay()
        fun onBroadcastNext()
        fun onBroadcastPrevious()
        fun onBroadcastLike()
        fun onBroadcastCancel()
    }
}