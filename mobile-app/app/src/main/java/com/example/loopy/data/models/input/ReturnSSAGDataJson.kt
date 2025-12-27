package com.example.loopy.data.models.input

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class ReturnSSAGDataJson(
    val glucose: String,
    val activity: String,
    val stress: String,
    val sleep: String
) {
    override fun toString(): String {
        return """
{
    "glucose": "$glucose",
    "activity": "$activity",
    "stress": "$stress",
    "sleep": "$sleep"
}
        """.trimIndent()
    }
}