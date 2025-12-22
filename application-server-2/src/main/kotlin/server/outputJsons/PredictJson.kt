package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable

@Serializable
class PredictJson(
    val userId: Int,
    val prediction: String,
) {
    override fun toString(): String {
        return """
             {
             "userId": $userId,
             "prediction": "$prediction"
             }
        """.trimIndent()
    }
}