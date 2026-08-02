package com.fitworkup.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActivityResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("distanceKm") val distanceKm: Double,
    @SerializedName("steps") val steps: Int,
    @SerializedName("avgSpeed") val avgSpeed: Double,
    @SerializedName("isValid") val isValid: Boolean,
    @SerializedName("status") val status: String,
    @SerializedName("acceptedSteps") val acceptedSteps: Int?,
    @SerializedName("heldSteps") val heldSteps: Int?,
    @SerializedName("riskScore") val riskScore: Int?
)