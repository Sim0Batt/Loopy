package com.example.loopy.login.models.input

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class UserJson (
    val email: String,
    val password: String,
){
    override fun toString(): String {
        return """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()
    }
}