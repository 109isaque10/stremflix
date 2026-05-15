package com.stremflix.data.di

import com.stremflix.data.local.PreferencesDataSource
import com.stremflix.data.local.StremflixDatabase
import com.stremflix.data.remote.OmdbApi
import com.stremflix.data.remote.StremioApi
import com.stremflix.data.remote.TmdbApi
import com.stremflix.data.remote.TraktApi
import com.stremflix.data.local.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideStremioApi(
        httpClient: HttpClient,
        preferencesDataSource: PreferencesDataSource
    ): StremioApi = StremioApi(httpClient, preferencesDataSource)

    @Provides
    @Singleton
    fun provideTraktApi(
        httpClient: HttpClient,
        preferencesDataSource: PreferencesDataSource
    ): TraktApi = TraktApi(httpClient, preferencesDataSource)

    @Provides
    @Singleton
    fun provideTmdbApi(
        httpClient: HttpClient,
        preferencesDataSource: PreferencesDataSource
    ): TmdbApi = TmdbApi(httpClient, preferencesDataSource)

    @Provides
    @Singleton
    fun provideOmdbApi(
        httpClient: HttpClient,
        preferencesDataSource: PreferencesDataSource
    ): OmdbApi = OmdbApi(httpClient, preferencesDataSource)
}