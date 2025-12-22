package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable
import kotlin.text.trimIndent

@Serializable
class ReturnDataJson(
    val heartRate: Double,
    val oxygen: Double,
    val sweating: Double,
    val temperature: Double,
    ) {
    override fun toString(): String {
        return """
            {
            "heartRate": "$heartRate",
            "oxygen": "$oxygen",
            "sweating": "$sweating",
            "temperature": "$temperature",
            }
        """.trimIndent()
    }
}