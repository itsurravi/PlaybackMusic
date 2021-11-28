package com.ravisharma.playbackmusic.di

import android.content.Context
import com.ravisharma.playbackmusic.database.PlaylistDatabase
import com.ravisharma.playbackmusic.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
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
    fun providePlaylistDatabase(@ApplicationContext context: Context) = PlaylistDatabase.getInstance(context)

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
}