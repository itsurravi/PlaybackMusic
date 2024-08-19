package com.ravisharma.playbackmusic.new_work.ui.fragments.now

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.R

enum class RepeatMode(@DrawableRes val iconResource: Int, @ColorRes val tintColor: Int) {
    NO_REPEAT(R.drawable.ic_repeat, R.color.white),
    REPEAT_ALL(R.drawable.ic_repeat, R.color.fav_on),
    REPEAT_ONE(R.drawable.ic_repeat_once, R.color.fav_on);

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