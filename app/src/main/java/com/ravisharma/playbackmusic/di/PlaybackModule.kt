package com.ravisharma.playbackmusic.di

import android.content.Context
import androidx.room.Room
import com.ravisharma.playbackmusic.database.MusicDatabase
import com.ravisharma.playbackmusic.provider.DataManager
import com.ravisharma.playbackmusic.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

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
    fun providesDataManager(
        db: MusicDatabase,
    ): DataManager {
        return DataManager(
            songDao = db.songDao(),
            albumDao = db.albumDao(),
            artistDao = db.artistDao(),
            albumArtistDao = db.albumArtistDao(),
            composerDao = db.composerDao(),
            lyricistDao = db.lyricistDao(),
            genreDao = db.genreDao(),
            playlistDao = db.playList2Dao(),
        )
    }
}