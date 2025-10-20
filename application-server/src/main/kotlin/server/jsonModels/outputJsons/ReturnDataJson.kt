package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable
import kotlin.text.trimIndent

@Serializable
class ReturnDataJson(
    val heartRates: String,
    val oxygens: String,
    val timestampsPPG: String,
    val sweatings: String,
    val timestampsElectrodes: String,
    val temperatures: String,
    val timestampsTermometer: String,
    val movements: String,
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
            "movement": "$movements"
            "timestampAccelerometer": "$timestampsAccelerometer"
            }
        """.trimIndent()
    }
}