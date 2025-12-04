package com.example.loopy.login.models.output

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class AccountJson(
    val userId: Int,
    val username: String
) {
    override fun toString(): String {
        return """
            {
                "userId":"$userId",
                "username":"$username"
            }
        """.trimIndent()
    }
}