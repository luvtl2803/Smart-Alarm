package com.anhq.smartalarm.core.data.di

import android.content.Context
import com.anhq.smartalarm.core.data.repository.AlarmHistoryRepository
import com.anhq.smartalarm.core.data.repository.AlarmRepository
import com.anhq.smartalarm.core.data.repository.AlarmSuggestionRepository
import com.anhq.smartalarm.core.data.repository.DefaultAlarmRepository
import com.anhq.smartalarm.core.data.repository.DeviceActivityRepository
import com.anhq.smartalarm.core.database.dao.AlarmHistoryDao
import com.anhq.smartalarm.core.database.dao.AlarmSuggestionDao
import com.anhq.smartalarm.core.database.dao.DeviceActivityDao
import com.anhq.smartalarm.core.utils.AlarmSuggestionAnalyzer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun provideAlarmRepository(default: DefaultAlarmRepository): AlarmRepository

    companion object {
        @Provides
        @Singleton
        fun providesAlarmHistoryRepository(
            alarmHistoryDao: AlarmHistoryDao
        ): AlarmHistoryRepository = AlarmHistoryRepository(alarmHistoryDao)

        @Provides
        @Singleton
        fun providesAlarmSuggestionAnalyzer(): AlarmSuggestionAnalyzer = AlarmSuggestionAnalyzer()

        @Provides
        @Singleton
        fun providesAlarmSuggestionRepository(
            alarmSuggestionDao: AlarmSuggestionDao,
            alarmHistoryDao: AlarmHistoryDao,
            suggestionAnalyzer: AlarmSuggestionAnalyzer
        ): AlarmSuggestionRepository = AlarmSuggestionRepository(
            alarmSuggestionDao,
            alarmHistoryDao,
            suggestionAnalyzer
        )

        @Provides
        @Singleton
        fun providesDeviceActivityRepository(
            @ApplicationContext context: Context,
            deviceActivityDao: DeviceActivityDao
        ): DeviceActivityRepository =
            DeviceActivityRepository(context, deviceActivityDao)
    }
}