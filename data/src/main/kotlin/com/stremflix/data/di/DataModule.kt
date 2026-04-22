package com.stremflix.data.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.repository.PreferencesRepository
import com.stremflix.core.repository.StreamRepository
import com.stremflix.core.repository.TraktRepository
import com.stremflix.core.repository.WatchProgressRepository
import com.stremflix.data.db.AppDatabase
import com.stremflix.data.network.TmdbService
import com.stremflix.data.network.TraktService
import com.stremflix.data.prefs.SettingsStore
import com.stremflix.data.repository.MetadataRepositoryImpl
import com.stremflix.data.repository.StreamRepositoryImpl
import com.stremflix.data.repository.TraktRepositoryImpl
import com.stremflix.data.repository.WatchProgressRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()

    @Provides
    @Singleton
    fun provideTmdbService(client: OkHttpClient, json: Json): TmdbService = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TmdbService::class.java)

    @Provides
    @Singleton
    fun provideTraktService(client: OkHttpClient, json: Json): TraktService = Retrofit.Builder()
        .baseUrl("https://api.trakt.tv/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TraktService::class.java)

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "stremflix.db"
    ).build()

    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): PreferencesRepository = SettingsStore(context)

    @Provides
    @Singleton
    fun provideMetadataRepository(
        tmdbService: TmdbService,
        db: AppDatabase,
        preferencesRepository: PreferencesRepository,
        json: Json
    ): MetadataRepository = MetadataRepositoryImpl(tmdbService, db.cacheDao(), preferencesRepository, json)

    @Provides
    @Singleton
    fun provideStreamRepository(
        client: OkHttpClient,
        preferencesRepository: PreferencesRepository,
        json: Json
    ): StreamRepository = StreamRepositoryImpl(client, preferencesRepository, json)

    @Provides
    @Singleton
    fun provideWatchProgressRepository(db: AppDatabase): WatchProgressRepository = WatchProgressRepositoryImpl(db.cacheDao())

    @Provides
    @Singleton
    fun provideTraktRepository(
        traktService: TraktService,
        preferencesRepository: PreferencesRepository
    ): TraktRepository = TraktRepositoryImpl(traktService, preferencesRepository)
}
