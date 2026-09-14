package com.fitworkup.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE activities ADD COLUMN route_json TEXT NOT NULL DEFAULT '[]'"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE activities ADD COLUMN owner_user_id TEXT NOT NULL DEFAULT ''"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_activities_owner_user_id " +
                    "ON activities(owner_user_id)"
            )
        }
    }

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }
}
