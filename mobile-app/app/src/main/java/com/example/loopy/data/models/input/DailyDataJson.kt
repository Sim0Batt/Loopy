package com.example.loopy.data.models.input

import kotlinx.serialization.Serializable

@Serializable
data class DailyDataJson(
        val heartRate: String,
        val oxygen: String,
        val sweating: String,
        val temperature: String,
        val glucose: String,
        val activity: String,
        val stress: String,
        val sleep: String
)
