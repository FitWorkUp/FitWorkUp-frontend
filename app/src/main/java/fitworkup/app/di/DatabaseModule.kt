package com.fitworkup.app.di

import android.content.Context
import androidx.room.Room
import com.fitworkup.app.data.local.dao.ActivityDao
import com.fitworkup.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fitworkup_db"
        )
            .fallbackToDestructiveMigration() // Em ambiente de desenvolvimento/teste
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }
}