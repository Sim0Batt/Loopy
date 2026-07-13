package server.outputJsons

import kotlinx.serialization.Serializable

@Serializable
class ReturnDataJson(
    val heartRates: String,
    val oxygens: String,
    val timestampsPPG: String,
    val sweatings: String,
    val timestampsElectrodes: String,
    val temperatures: String,
    val timestampsTermometer: String,
    val acc_x: String,
    val acc_y: String,
    val acc_z: String,
    val timestampsAccelerometer: String,
) {
    override fun toString(): String {
        return """
            {
            "heartRate": "$heartRates",
            "oxygen": "$oxygens",
            "timestampPPG": "$timestampsPPG",
            "sweating": "$sweatings",
            "timestampElectrodes": "$timestampsElectrodes",
            "temperature": "$temperatures",
            "timestampTermometer": "$timestampsTermometer"
            "acc_x": "$acc_x"
            "acc_y": "$acc_y"
            "acc_z": "$acc_z"
            "timestampAccelerometer": "$timestampsAccelerometer"
            }
        """.trimIndent()
    }
}