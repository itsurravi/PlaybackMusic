package com.ravisharma.playbackmusic.new_work.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

fun Activity.changeStatusBarColor(@ColorRes color: Int) {
//    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//    window.statusBarColor = ContextCompat.getColor(this, color)
}

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

fun View.linearGradientBackground(dominantColor: Int): GradientDrawable {

    return GradientDrawable().apply {
        colors = intArrayOf(
            dominantColor,
            dominantColor,
            Color.parseColor("#101d25"),
        )
        gradientType = GradientDrawable.LINEAR_GRADIENT
        orientation = GradientDrawable.Orientation.TOP_BOTTOM

    }
}