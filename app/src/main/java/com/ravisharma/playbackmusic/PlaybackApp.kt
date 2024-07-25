package com.ravisharma.playbackmusic

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlaybackApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        /*MobileAds.initialize(this, initializationStatus -> {

        });*/
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).apply {
            allowRgb565(true)
            bitmapConfig(Bitmap.Config.RGB_565)
            error(R.drawable.logo)
        }.build()
    }
}
