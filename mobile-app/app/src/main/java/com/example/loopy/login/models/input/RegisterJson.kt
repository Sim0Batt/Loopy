package com.example.loopy.login.models.input

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class RegisterJson (
    val username: String,
    val email: String,
    val password: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val sex: String
){
    override fun toString(): String {
        return """
            {
                "email": "$email",
                "password": "$password",
                "username": "$username",
                "age": $age,
                "height": $height,
                "weight": $weight,
                "sex": $sex
            }
        """.trimIndent()
    }
}