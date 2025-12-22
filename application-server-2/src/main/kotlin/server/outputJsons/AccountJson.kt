package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable


@Serializable
class AccountJson(
    val userId: Int,
    val username: String,
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