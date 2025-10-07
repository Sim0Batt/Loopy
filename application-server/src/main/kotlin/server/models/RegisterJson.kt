package server.models

import kotlinx.serialization.Serializable

@Serializable
class RegisterJson (
    val email: String,
    val password: String,
    val username: String,
    val age: Int,
    val weight: Int,
    val height: Int,
    val sex: String
){
    override fun toString(): String {
        return """
            email: $email,
            password: $password,
            username: $username,
            age: $age,
            weight: $weight,
            height: $height,
            sex: $sex
        """.trimIndent()
    }
}