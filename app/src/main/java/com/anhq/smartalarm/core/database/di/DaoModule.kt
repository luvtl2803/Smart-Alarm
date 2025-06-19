package com.anhq.smartalarm.core.database.di

import com.anhq.smartalarm.core.database.AppDatabase
import com.anhq.smartalarm.core.database.dao.TimerDao
import com.anhq.smartalarm.core.database.dao.AlarmDao
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.AlarmSuggestionDao
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    @Singleton
    fun providesAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()

    @Provides
    @Singleton
    fun providesTimerDao(database: AppDatabase): TimerDao = database.timerDao()

    @Provides
    @Singleton
    fun providesAlarmHistoryDao(database: AppDatabase): AlarmHistoryDao = database.alarmHistoryDao()

    @Provides
    @Singleton
    fun providesAlarmSuggestionDao(database: AppDatabase): AlarmSuggestionDao = database.alarmSuggestionDao()

    @Provides
    @Singleton
    fun providesDeviceActivityDao(database: AppDatabase) : DeviceActivityDao = database.deviceActivityDao()
}