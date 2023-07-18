package com.ravisharma.playbackmusic.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.database.PlaylistDatabase
import com.ravisharma.playbackmusic.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.new_work.notification.PlaybackNotificationManager
import com.ravisharma.playbackmusic.new_work.services.DataManager
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePrefManager(@ApplicationContext context: Context) = PrefManager(context)

    @Provides
    @Singleton
    fun provideTinyDb(@ApplicationContext context: Context) = TinyDB(context)

    @Provides
    @Singleton
    fun providePlaylistDatabase(@ApplicationContext context: Context) =
        PlaylistDatabase.getInstance(context)

    @Provides
    @Singleton
    fun providePlaylistDao(database: PlaylistDatabase) = database.playlistDao()

    @Provides
    @Singleton
    fun provideMostPlayedDao(database: PlaylistDatabase) = database.mostPlayedDao()

    @Provides
    @Singleton
    fun provideLastPlayedDao(database: PlaylistDatabase) = database.lastPlayedDao()

    @Provides
    @Singleton
    fun providePlaylistRepository(playlistDao: PlaylistDao) = PlaylistRepository(playlistDao)

    @Provides
    @Singleton
    fun providerNotificationManager(
        @ApplicationContext context: Context
    ): PlaybackNotificationManager {
        return PlaybackNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideDataManger(
        @ApplicationContext context: Context,
        dataProvider: DataProvider
    ): DataManager {
        return DataManager(context, dataProvider)
    }

    @Singleton
    @Provides
    fun providesExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
//        val extractorsFactory = DefaultExtractorsFactory().apply {
//            setMp3ExtractorFlags(Mp3Extractor.FLAG_DISABLE_ID3_METADATA)
//        }
        val audioAttributes = AudioAttributes.Builder().apply {
            setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            setUsage(C.USAGE_MEDIA)
        }.build()

        return ExoPlayer.Builder(context).apply {
//            setMediaSourceFactory(DefaultMediaSourceFactory(context,extractorsFactory))
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }.build()
    }
}