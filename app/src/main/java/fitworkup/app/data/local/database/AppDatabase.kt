package com.fitworkup.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fitworkup.app.data.local.dao.ActivityDao
import com.fitworkup.app.data.local.entity.ActivityEntity

@Database(
    entities = [ActivityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}