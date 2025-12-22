package com.example.loopy.data.models.input

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ReturnDataJson(
        val heartRate: String,
        val oxygen: String,
        val sweating: String,
        val temperature: String,
)
