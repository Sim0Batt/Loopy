package server.outputJsons

import kotlinx.serialization.Serializable

@Serializable
class ReturnDataJson(
    val heartRate: Double,
    val oxygen: Double,
    val sweating: Double,
    val temperature: Double,
    val glucose: Double,
    val activity: String,
    val stress: String,
    val sleep: String
    ) {
    override fun toString(): String {
        return """
            {
            "heartRate": "$heartRate",
            "oxygen": "$oxygen",
            "sweating": "$sweating",
            "temperature": "$temperature",
            "glucose": "$glucose",
            "activity": "$activity",
            "stress": "$stress",
            "sleep": "$sleep"
            }
        """.trimIndent()
    }
}