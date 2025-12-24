package server.inputJsons

import kotlinx.serialization.Serializable

@Serializable
class AgentJson (
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