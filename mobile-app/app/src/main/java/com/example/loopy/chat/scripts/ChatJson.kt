package com.example.loopy.chat.scripts

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
class ChatJson(
    val input: String,
    val userId: Int
) {
    override fun toString(): String {
        return """
            {
                "input": "$input",
                "userId": "$userId"
            }
        """.trimIndent()
    }
}