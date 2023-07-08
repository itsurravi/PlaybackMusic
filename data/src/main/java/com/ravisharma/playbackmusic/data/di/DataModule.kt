package com.ravisharma.playbackmusic.data.di

import android.content.Context
import androidx.room.Room
import com.ravisharma.playbackmusic.data.db.MusicDatabase
import com.ravisharma.playbackmusic.data.provider.DataProvider
import com.ravisharma.playbackmusic.data.provider.DataScanner
import com.ravisharma.playbackmusic.data.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun providesDataScanner(
        db: MusicDatabase,
    ): DataScanner {
        return DataScanner(
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
    fun providesDataProvider(
        @ApplicationContext context: Context,
        db: MusicDatabase,
    ): DataProvider {
        return DataProvider(
            context = context,
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
}