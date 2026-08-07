package com.fitworkup.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "distance_km")
    val distanceKm: Double,

    @ColumnInfo(name = "steps")
    val steps: Int,

    @ColumnInfo(name = "avg_speed")
    val avgSpeed: Double,

    @ColumnInfo(name = "accepted_steps")
    val acceptedSteps: Int,

    @ColumnInfo(name = "held_steps")
    val heldSteps: Int,

    @ColumnInfo(name = "risk_score")
    val riskScore: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)