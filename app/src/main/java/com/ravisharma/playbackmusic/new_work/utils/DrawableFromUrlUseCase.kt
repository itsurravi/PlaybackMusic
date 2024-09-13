package com.ravisharma.playbackmusic.new_work.utils

import android.content.Context
import android.graphics.drawable.Drawable
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.ravisharma.playbackmusic.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrawableFromUrlUseCase {

    suspend fun invoke(
        urlString: String, context: Context
    ): Result<Drawable> = withContext(Dispatchers.IO) {
        val imageRequest = ImageRequest.Builder(context)
            .data(urlString)
            .error(R.drawable.logo)
            .allowHardware(false)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
        // Each ImageLoader instance has its own memory & disk cache. Therefore,
        // use a singleton instead of creating an instance of ImageLoader
        // everytime this function is invoked.
        when (val imageResult = context.imageLoader.execute(imageRequest)) {
            is SuccessResult -> Result.success(imageResult.drawable)
//            is ErrorResult -> Result.failure(imageResult.throwable)
            is ErrorResult -> Result.success(imageResult.request.error!!)
        }
    }

}