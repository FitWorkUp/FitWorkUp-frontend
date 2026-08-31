package com.fitworkup.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitworkup.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Query("UPDATE activities SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("SELECT * FROM activities WHERE is_synced = 0")
    suspend fun getUnsyncedActivities(): List<ActivityEntity>

    // 💡 Consulta para alimentar o Calendário e o Gráfico Mensal
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivitiesFlow(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    suspend fun getAllActivities(): List<ActivityEntity>

    @Query("SELECT MAX(timestamp) FROM activities")
    suspend fun getLatestActivityTimestamp(): Long?
}
