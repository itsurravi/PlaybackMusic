package com.ravisharma.playbackmusic

import android.app.Application
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlaybackApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {  }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this).apply {
            allowRgb565(true)
            bitmapConfig(Bitmap.Config.RGB_565)
            error(R.drawable.logo)
            memoryCache {
                MemoryCache.Builder(this@PlaybackApp)
                    .maxSizePercent(0.20)
                    .build()
            }
            diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(5 * 1024 * 1024)
                    .build()
            }
        }.build()
    }
}
