package com.ravisharma.playbackmusic.new_work.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DynamicThemeManager @Inject constructor(
    private val downloadDrawableFromUrlUseCase: DrawableFromUrlUseCase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val colorCache = LruCache<String, Int?>(10)

    private fun Int.toHexCode(): String {
        return java.lang.String.format("#%06X", (0xFFFFFF and this))
    }

    private suspend fun getBackgroundColorForBitmap(bitmap: Bitmap): Int? =
        withContext(defaultDispatcher) {
            val palette = Palette.from(bitmap).generate()
            /*val dominantSwatch = palette.dominantSwatch?.hsl
            val vibrantSwatch = palette.vibrantSwatch?.hsl
            val darkVibrantSwatch = palette.darkVibrantSwatch?.hsl
            val lightVibrantSwatch = palette.lightVibrantSwatch?.hsl
            val mutedSwatch = palette.mutedSwatch?.hsl
            val darkMutedSwatch = palette.darkMutedSwatch?.hsl
            val lightMutedSwatch = palette.lightMutedSwatch?.hsl

            Log.i(
                "Colors",
                "${dominantSwatch?.toList()} ${vibrantSwatch?.toList()} ${darkVibrantSwatch?.toList()} ${lightVibrantSwatch?.toList()} ${mutedSwatch?.toList()} ${darkMutedSwatch?.toList()} ${lightMutedSwatch?.toList()}"
            )*/

            val color =
                /*palette.lightVibrantSwatch ?: palette.vibrantSwatch ?: */palette.dominantSwatch
            color?.hsl
                ?.apply {
                    // set the brightness of the dominant color to 50%
                    this[2] = 0.20f
                }
                ?.let(ColorUtils::HSLToColor)
        }

    suspend fun getBackgroundColorForImageFromUrl(url: String, context: Context): Int? {
        if (colorCache.get(url) != null) return colorCache.get(url)
        val bitmap = downloadDrawableFromUrlUseCase.invoke(url, context)
            .getOrNull()
            ?.toBitmap() ?: return null
        return getBackgroundColorForBitmap(bitmap).also {
            colorCache.put(url, it)
        }
    }
}

