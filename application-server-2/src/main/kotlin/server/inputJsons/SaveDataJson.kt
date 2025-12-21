package server.jsonModels.inputJsons

import kotlinx.serialization.Serializable
import kotlin.text.trimIndent

@Serializable
class SaveDataJson(
    val heartRate: Int,
    val oxygen: Double,
    val timestampPPG: String,
    val sweating: Double,
    val timestampElectrodes: String,
    val temperature: Double,
    val timestampTermometer: String,

    // NUOVI CAMPI (x,y,z)
    val acc_x: Double,
    val acc_y: Double,
    val acc_z: Double,

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
            "timestampTermometer": "$timestampTermometer",
            "acc_x": "$acc_x",
            "acc_y": "$acc_y",
            "acc_z": "$acc_z",
            "timestampAccelerometer": "$timestampAccelerometer"
            }
        """.trimIndent()
    }
}