package com.example.loopy.chat.scripts

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
class ChatJson(
    val input: String,
    val username: String
) {
    override fun toString(): String {
        return """
            {
                "input": "$input",
                "username": "$username"
            }
        """.trimIndent()
    }
}