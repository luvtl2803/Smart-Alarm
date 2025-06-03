package com.anhq.smartalarm.core.sharereference.di

import android.content.Context
import com.anhq.smartalarm.core.sharereference.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SfModule {

    @Provides
    @Singleton
    fun providePreferenceHelper(
        @ApplicationContext context: Context
    ): PreferenceHelper {
        return PreferenceHelper(context)
    }
}