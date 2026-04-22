package com.stremflix.app.di

import com.stremflix.core.repository.MetadataRepository
import com.stremflix.core.repository.StreamRepository
import com.stremflix.core.repository.TraktRepository
import com.stremflix.core.usecase.FetchTraktRowsUseCase
import com.stremflix.core.usecase.GetTrailerUseCase
import com.stremflix.core.usecase.ObserveHomeRowsUseCase
import com.stremflix.core.usecase.ResolveStreamUseCase
import com.stremflix.core.usecase.SearchMediaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideObserveHome(metadataRepository: MetadataRepository) = ObserveHomeRowsUseCase(metadataRepository)

    @Provides
    @Singleton
    fun provideSearch(metadataRepository: MetadataRepository) = SearchMediaUseCase(metadataRepository)

    @Provides
    @Singleton
    fun provideTrailer(metadataRepository: MetadataRepository) = GetTrailerUseCase(metadataRepository)

    @Provides
    @Singleton
    fun provideResolveStream(streamRepository: StreamRepository) = ResolveStreamUseCase(streamRepository)

    @Provides
    @Singleton
    fun provideTraktRows(traktRepository: TraktRepository) = FetchTraktRowsUseCase(traktRepository)
}
