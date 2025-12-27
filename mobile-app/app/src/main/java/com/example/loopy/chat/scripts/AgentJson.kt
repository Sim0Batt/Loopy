package com.example.loopy.chat.scripts

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
class AgentJson(
    val input: String,
) {
    override fun toString(): String {
        return """
            {
                "input": "$input"
            }
        """.trimIndent()
    }
}