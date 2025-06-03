package com.anhq.smartalarm.core.database.di

import com.anhq.smartalarm.core.database.AlarmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DaoModule {
    @Provides
    fun providesAlarmDao(database: AlarmDatabase) = database.alarmDao()
}