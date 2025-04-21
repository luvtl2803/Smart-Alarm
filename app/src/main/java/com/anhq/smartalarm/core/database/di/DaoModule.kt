package com.anhq.smartalarm.core.database.di

import com.anhq.smartalarm.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaoModule {
    @Provides
    fun providesTaskDao(database: AppDatabase) = database.alarmDao()
}