package server.models

import kotlinx.serialization.Serializable

@Serializable
class UserJson (
    val email: String,
    val password: String
){
    override fun toString(): String {
        return """
            email: $email,
            password: $password
        """.trimIndent()
    }
}