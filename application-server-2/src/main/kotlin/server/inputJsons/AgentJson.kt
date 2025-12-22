package server.jsonModels.inputJsons

import kotlinx.serialization.Serializable

@Serializable
class AgentJson (
    val username: String,
    val input: String,
){
    override fun toString(): String {
        return """
            {
            "input": "$input"
            }
        """.trimIndent()
    }
}