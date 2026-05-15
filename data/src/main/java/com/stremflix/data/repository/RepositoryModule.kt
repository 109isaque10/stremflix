package com.stremflix.data.repository

import android.content.Context
import com.stremflix.core.util.AppDispatchers
import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.local.dao.ContentDao
import com.stremflix.data.manager.TraktOAuthManager
import com.stremflix.data.remote.StremioApi
import com.stremflix.data.remote.TmdbApi
import com.stremflix.data.remote.TraktApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContentRepository(
        tmdbApi: TmdbApi,
        contentDao: ContentDao
    ): ContentRepository = ContentRepository(tmdbApi, contentDao)

    @Provides
    @Singleton
    fun provideStreamRepository(
        stremioApi: StremioApi,
        preferencesDataSource: PreferencesDataSource
    ): StreamRepository = StreamRepository(stremioApi, preferencesDataSource)

    @Provides
    @Singleton
    fun provideTraktRepository(
        traktApi: TraktApi,
        oAuthManager: TraktOAuthManager,
        contentDao: ContentDao, // ADD THIS
        dispatchers: AppDispatchers // ADD THIS
    ): TraktRepository = TraktRepository(traktApi, oAuthManager, contentDao, dispatchers)

    @Provides
    @Singleton
    fun providePreferencesRepository(
        dataSource: PreferencesDataSource
    ): PreferencesRepository = PreferencesRepository(dataSource)

    @Provides
    @Singleton
    fun provideTraktTokenRepository(
        encryptedPrefs: androidx.security.crypto.EncryptedSharedPreferences
    ): TraktTokenRepository = TraktTokenRepository(encryptedPrefs)

    @Provides
    @Singleton
    fun provideTraktOAuthManager(
        @ApplicationContext context: Context,
        api: TraktApi,
        prefs: PreferencesDataSource,
        tokens: TraktTokenRepository,
        dispatchers: AppDispatchers
    ): TraktOAuthManager = TraktOAuthManager(context, api, prefs, tokens, dispatchers)
}