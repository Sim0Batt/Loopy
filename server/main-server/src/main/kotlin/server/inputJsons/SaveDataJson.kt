package server.inputJsons

import kotlinx.serialization.Serializable

@Serializable
class SaveDataJson(
    val heartRate: Int,
    val oxygen: Double,
    val timestampPPG: String,
    val sweating: Double,
    val timestampElectrodes: String,
    val temperature: Double,
    val timestampTermometer: String,
    val movement: Boolean,
    val timestampAccelerometer: String,
) {
    override fun toString(): String {
        return """
            {
            "heartRate": "$heartRate",
            "oxygen": "$oxygen",
            "timestampPPG": "$timestampPPG",
            "sweating": "$sweating",
            "timestampElectrodes": "$timestampElectrodes",
            "temperature": "$temperature",
            "timestampTermometer": "$timestampTermometer"
            "movement": "$movement"
            "timestampAccelerometer": "$timestampAccelerometer"
            }
        """.trimIndent()
    }
}