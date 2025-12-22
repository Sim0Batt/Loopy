package server.jsonModels.inputJsons

import kotlinx.serialization.Serializable

@Serializable
class AgentJson (
    val userId: Int,
    val input: String,
){
    override fun toString(): String {
        return """
            {
            "userId": "$userId"
            "input": "$input"
            }
        """.trimIndent()
    }
}