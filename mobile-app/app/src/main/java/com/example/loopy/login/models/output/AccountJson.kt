package com.example.loopy.login.models.output

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class AccountJson(
    val userId: Int
) {
    override fun toString(): String {
        return """
            {
                "userId":"$userId"
            }
        """.trimIndent()
    }
}