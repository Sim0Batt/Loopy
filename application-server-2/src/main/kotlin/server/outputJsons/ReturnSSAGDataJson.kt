package com.example.loopy.data.models.input

import kotlinx.serialization.Serializable

@Serializable
class ReturnSSAGDataJson(
    val glucose: Double,
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