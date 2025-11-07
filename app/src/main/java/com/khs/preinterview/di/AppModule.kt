package com.khs.preinterview.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.khs.preinterview.data.local.db.AppDatabase
import com.khs.preinterview.data.local.db.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

/**
 * Hilt 모듈: 의존성 주입을 위해 필요한 객체들을 생성하고 제공합니다.
 * SingletonComponent에 설치되어 앱의 생명주기 동안 단 하나의 인스턴스만 유지됩니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_db"
        ).build()
    }

    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}

