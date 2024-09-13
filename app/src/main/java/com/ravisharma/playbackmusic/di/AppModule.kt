package com.ravisharma.playbackmusic.di

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import com.ravisharma.playbackmusic.data.db.MusicDatabase
import com.ravisharma.playbackmusic.database.PlaylistDatabase
import com.ravisharma.playbackmusic.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.new_work.utils.Constants
import com.ravisharma.playbackmusic.new_work.data_proto.QueueState
import com.ravisharma.playbackmusic.new_work.data_proto.QueueStateSerializer
import com.ravisharma.playbackmusic.new_work.services.PlaybackBroadcastReceiver
import com.ravisharma.playbackmusic.new_work.services.data.PlayerService
import com.ravisharma.playbackmusic.new_work.services.data.PlayerServiceImpl
import com.ravisharma.playbackmusic.new_work.services.data.PlaylistService
import com.ravisharma.playbackmusic.new_work.services.data.PlaylistServiceImpl
import com.ravisharma.playbackmusic.new_work.services.data.QueueService
import com.ravisharma.playbackmusic.new_work.services.data.QueueServiceImpl
import com.ravisharma.playbackmusic.new_work.services.data.SearchService
import com.ravisharma.playbackmusic.new_work.services.data.SearchServiceImpl
import com.ravisharma.playbackmusic.new_work.services.data.SleepTimerService
import com.ravisharma.playbackmusic.new_work.services.data.SleepTimerServiceImpl
import com.ravisharma.playbackmusic.new_work.services.data.SongService
import com.ravisharma.playbackmusic.new_work.services.data.SongServiceImpl
import com.ravisharma.playbackmusic.new_work.utils.DrawableFromUrlUseCase
import com.ravisharma.playbackmusic.new_work.utils.DynamicThemeManager
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

typealias DataConstants = com.ravisharma.playbackmusic.data.utils.Constants

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

    @SuppressLint("UnsafeOptInUsageError")
    @Singleton
    @Provides
    fun providesExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        val extractorsFactory = DefaultExtractorsFactory().apply {
            setMp3ExtractorFlags(Mp3Extractor.FLAG_DISABLE_ID3_METADATA)
        }
        val audioAttributes = AudioAttributes.Builder().apply {
            setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            setUsage(USAGE_MEDIA)
        }.build()
        return ExoPlayer.Builder(context).apply {
            setMediaSourceFactory(DefaultMediaSourceFactory(context, extractorsFactory))
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }.build()
    }

    @Singleton
    @Provides
    @Named(value = "lastPlayedInfo")
    fun provideSharedPref(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences(Constants.LocalPref, Context.MODE_PRIVATE)


    @Singleton
    @Provides
    fun providesQueueStateDatastore(
        @ApplicationContext context: Context,
    ): DataStore<QueueState> {
        return DataStoreFactory.create(
            serializer = QueueStateSerializer,
            produceFile = {
                context.dataStoreFile(DataConstants.QUEUE_STATE_FILE)
            }
        )
    }


    @Singleton
    @Provides
    fun providesPlaylistService(
        db: MusicDatabase
    ): PlaylistService {
        return PlaylistServiceImpl(
            playlistDao = db.playListDao(),
        )
    }

    @Singleton
    @Provides
    fun providesSongService(
        db: MusicDatabase
    ): SongService {
        return SongServiceImpl(
            songDao = db.songDao(),
            albumDao = db.albumDao(),
            artistDao = db.artistDao(),
            albumArtistDao = db.albumArtistDao(),
            composerDao = db.composerDao(),
            lyricistDao = db.lyricistDao(),
            genreDao = db.genreDao(),
        )
    }

    @Singleton
    @Provides
    fun providesQueueService(): QueueService {
        return QueueServiceImpl()
    }

    @Singleton
    @Provides
    fun providesPlayerService(
        @ApplicationContext context: Context,
        queueService: QueueService,
    ): PlayerService {
        return PlayerServiceImpl(
            context = context,
            queueService = queueService,
        )
    }

    @Singleton
    @Provides
    fun providesSearchService(
        db: MusicDatabase,
    ): SearchService {
        return SearchServiceImpl(
            songDao = db.songDao(),
            albumDao = db.albumDao(),
            artistDao = db.artistDao(),
            albumArtistDao = db.albumArtistDao(),
            composerDao = db.composerDao(),
            lyricistDao = db.lyricistDao(),
            genreDao = db.genreDao(),
            playlistDao = db.playListDao()
        )
    }

    @Singleton
    @Provides
    fun providesSleepTimerService(
        @ApplicationContext context: Context,
    ): SleepTimerService {
        return SleepTimerServiceImpl(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            closeIntent = PendingIntent.getBroadcast(
                context, PlaybackBroadcastReceiver.CANCEL_ACTION_REQUEST_CODE,
                Intent(Constants.PACKAGE_NAME).putExtra(
                    PlaybackBroadcastReceiver.AUDIO_CONTROL,
                    PlaybackBroadcastReceiver.PLAYER_CANCEL
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    @Singleton
    @Provides
    fun provideThemeManager(): DynamicThemeManager {
        return DynamicThemeManager(DrawableFromUrlUseCase(), Dispatchers.IO)
    }
}