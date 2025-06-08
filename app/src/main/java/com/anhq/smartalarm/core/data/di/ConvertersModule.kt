package com.anhq.smartalarm.core.data.di

import com.anhq.smartalarm.core.database.converter.AlarmGameTypeConverter
import com.anhq.smartalarm.core.database.converter.DayOfWeekSetConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConvertersModule {
    @Singleton
    @Provides
    fun provideDayOfWeekSetConverter(): DayOfWeekSetConverter {
        return DayOfWeekSetConverter()
    }

    @Singleton
    @Provides
    fun provideAlarmGameTypeConverter(): AlarmGameTypeConverter {
        return AlarmGameTypeConverter()
    }
} 