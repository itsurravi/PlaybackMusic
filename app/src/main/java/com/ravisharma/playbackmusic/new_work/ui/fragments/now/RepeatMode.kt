package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import androidx.annotation.DrawableRes
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.R

enum class RepeatMode(@DrawableRes val iconResource: Int) {
    NO_REPEAT(R.drawable.ic_repeat_off),
    REPEAT_ALL(R.drawable.ic_repeat_all),
    REPEAT_ONE(R.drawable.ic_repeat_one);

    fun next(): RepeatMode {
        return when(this){
            NO_REPEAT -> REPEAT_ALL
            REPEAT_ALL -> REPEAT_ONE
            REPEAT_ONE -> NO_REPEAT
        }
    }

    fun toExoPlayerRepeatMode(): Int {
        return when(this){
            NO_REPEAT -> ExoPlayer.REPEAT_MODE_OFF
            REPEAT_ALL -> ExoPlayer.REPEAT_MODE_ALL
            REPEAT_ONE -> ExoPlayer.REPEAT_MODE_ONE
        }
    }
}