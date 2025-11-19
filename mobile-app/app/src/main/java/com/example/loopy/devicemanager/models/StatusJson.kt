package com.example.loopy.devicemanager.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class StatusJson(
    val accelerometerStatus: String,
    val thermometerStatus: String,
    val ppgStatus: String,
    val electrodesStatus: String,
    val timestamp: String,
) {
    override fun toString(): String {
        return """
{
    "accelerometerStatus": "$accelerometerStatus",
    "thermometerStatus": "$thermometerStatus",
    "ppgStatus": "$ppgStatus",
    "electrodesStatus": "$electrodesStatus"
    "timestamp": "$timestamp"
}            
        """.trimIndent()
    }
}