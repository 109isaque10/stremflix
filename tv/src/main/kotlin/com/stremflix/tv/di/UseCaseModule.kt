package com.stremflix.tv.di

import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.usecase.GetTrailerUseCase
import com.stremflix.core.usecase.ObserveHomeRowsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TvUseCaseModule {
    @Provides
    @Singleton
    fun provideObserveHome(metadataRepository: MetadataRepository) = ObserveHomeRowsUseCase(metadataRepository)

    @Provides
    @Singleton
    fun provideTrailer(metadataRepository: MetadataRepository) = GetTrailerUseCase(metadataRepository)
}
