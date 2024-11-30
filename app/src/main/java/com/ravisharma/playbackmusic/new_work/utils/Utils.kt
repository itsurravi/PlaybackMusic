package com.ravisharma.playbackmusic.new_work.utils

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/*fun View.changeSystemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            top = bars.top,
            bottom = bars.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}*/

fun View.changeStatusBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.statusBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            top = bars.top,
        )
        WindowInsetsCompat.CONSUMED
    }
}

fun View.changeNavigationBarPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.navigationBars()
        )
        v.updatePadding(
            bottom = bars.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}

fun View.changeStatusBarMargin() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.statusBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = bars.top
        }
        WindowInsetsCompat.CONSUMED
    }
}

fun View.linearGradientBackground(dominantColor: Int) {
    val newBackground = GradientDrawable().apply {
        colors = intArrayOf(
            dominantColor,
            dominantColor,
//            Color.parseColor("#101d25"),
        )
        gradientType = GradientDrawable.LINEAR_GRADIENT
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
    }
    val transitionDrawable = TransitionDrawable(arrayOf(background, newBackground))
    background = transitionDrawable
    transitionDrawable.startTransition(200)
}