package server.models

import kotlinx.serialization.Serializable

@Serializable
class AgentJson (
    val username: String,
    val input: String,
){
    override fun toString(): String {
        return """
            {
            "username": "$username",
            "input": "$input"
            }
        """.trimIndent()
    }
}