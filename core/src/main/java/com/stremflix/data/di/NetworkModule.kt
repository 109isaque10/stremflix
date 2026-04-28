package com.stremflix.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.stremflix.data.remote.TmdbApi
import com.stremflix.data.remote.StremioApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
private const val STREMIO_BASE_URL = "http://127.0.0.1:7000/" // Local proxy usually required

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(sharedPrefs: SharedPreferences): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        
        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            // Add TMDB Auth if needed
            val tmdbApiKey = sharedPrefs.getString("tmdb_api_key", "")
            val newUrl = request.url.newBuilder()
                .addQueryParameter("api_key", tmdbApiKey)
                .addQueryParameter("language", java.util.Locale.getDefault().language)
                .build()
            val newRequest = request.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi {
        return retrofit.create(TmdbApi::class.java)
    }
    
    // Note: Stremio API requires a separate Retrofit instance usually due to different base URLs
    // and lack of standard auth. Simplified here.
    @Provides
    @Singleton
    fun provideStremioRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(STREMIO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}