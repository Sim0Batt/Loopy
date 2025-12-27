package com.example.loopy.profile.json

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class UserDataJson(
    val username: String,
    val email: String,
    val age: String,
    val gender: String,
    val weight: String,
    val height: String,
) {
    override fun toString(): String {
        return """
{
    "username": "$username",
    "email": "$email",
    "age": "$age",
    "gender": "$gender",
    "weight": "$weight",
    "height": "$height"
}
        """.trimIndent()
    }
}