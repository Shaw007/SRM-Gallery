package com.srmstudios.srmgallery.di

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
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
    fun providesContentResolver(application: Application) =
        application.contentResolver

    @Provides
    @Singleton
    fun providesImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .availableMemoryPercentage(1.0)
            .logger(DebugLogger())
            .build()
    }
}