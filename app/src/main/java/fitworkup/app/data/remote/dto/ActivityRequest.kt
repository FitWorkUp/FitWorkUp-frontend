package com.fitworkup.app.data.remote.dto

import com.fitworkup.app.domain.model.RoutePoint
import com.google.gson.annotations.SerializedName

data class ActivityRequest(
    @SerializedName("type")
    val type: String,

    @SerializedName("distanceKm")
    val distanceKm: Double,

    @SerializedName("steps")
    val steps: Int,

    @SerializedName("avgSpeed")
    val avgSpeed: Double,

    @SerializedName("acceptedSteps")
    val acceptedSteps: Int,

    @SerializedName("heldSteps")
    val heldSteps: Int,

    @SerializedName("riskScore")
    val riskScore: Int,

    @SerializedName("fraudReasons")
    val fraudReasons: List<String>,

    @SerializedName("routePoints")
    val routePoints: List<RoutePoint> = emptyList(),

    @SerializedName("plannedExerciseSessionId")
    val plannedExerciseSessionId: String? = null,

    @SerializedName("avgHeartRate")
    val avgHeartRate: Int? = null,

    @SerializedName("targetsAchieved")
    val targetsAchieved: Boolean? = null
)
