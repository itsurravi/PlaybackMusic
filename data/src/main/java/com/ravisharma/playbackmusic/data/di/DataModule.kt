package com.ravisharma.playbackmusic.data.di

import android.content.Context
import androidx.room.Room
import com.ravisharma.playbackmusic.data.components.DaoCollection
import com.ravisharma.playbackmusic.data.db.MusicDatabase
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.data.provider.SongExtractor
import com.ravisharma.playbackmusic.data.utils.Constants
import com.ravisharma.playbackmusic.data.utils.TinyDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun providesAppDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MusicDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()
    }

    @Singleton
    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Singleton
    @Provides
    fun providesSongExtractor(
        @ApplicationContext context: Context,
        db: MusicDatabase,
    ): SongExtractor {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        return SongExtractor(
            scope = scope,
            context = context,
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
    fun provideTinyDB(
        @ApplicationContext context: Context,
    ): TinyDb {
        return TinyDb(context)
    }
}