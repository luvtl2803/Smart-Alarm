package com.anhq.smartalarm.core.database.di

import android.content.Context
import androidx.room.Room
import com.anhq.smartalarm.core.database.AlarmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun providesAlarmDatabase(
        @ApplicationContext context: Context
    ): AlarmDatabase = Room.databaseBuilder(
        context,
        AlarmDatabase::class.java,
        "alarm-database"
    ).build()
}