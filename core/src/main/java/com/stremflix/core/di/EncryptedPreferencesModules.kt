package com.stremflix.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Named

private const val SECURE_PREFS_NAME = "stremflix_secure"
const val ENCRYPTED_PREFS = "encrypted_prefs"

@Module
@InstallIn(SingletonComponent::class)
object EncryptedPreferencesModule {

    @Provides
    @Singleton
    @Named(ENCRYPTED_PREFS)
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): androidx.security.crypto.EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as androidx.security.crypto.EncryptedSharedPreferences
    }
}