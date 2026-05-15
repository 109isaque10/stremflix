package com.stremflix.data.di

import android.content.Context
import androidx.room.Room
import com.stremflix.core.util.ContentLoadManager
import com.stremflix.data.local.StremflixDatabase
import com.stremflix.data.local.dao.ContentDao
import com.stremflix.data.local.dao.EpisodeDao
import com.stremflix.data.local.dao.TraktTokenDao
import com.stremflix.data.local.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StremflixDatabase = Room.databaseBuilder(
        context,
        StremflixDatabase::class.java,
        "stremflix_db"
    )
        // Use fallbackToDestructiveMigration for development.
        // For production, define explicit migrations.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideContentDao(database: StremflixDatabase): ContentDao = database.contentDao()

    @Provides
    @Singleton
    fun provideEpisodeDao(database: StremflixDatabase): EpisodeDao = database.episodeDao()

    @Provides
    @Singleton
    fun provideTraktTokenDao(database: StremflixDatabase): TraktTokenDao = database.traktTokenDao()

    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: StremflixDatabase): WatchHistoryDao = database.watchHistoryDao()

    @Provides
    @Singleton
    fun provideContentLoadManager(): ContentLoadManager = ContentLoadManager()
}