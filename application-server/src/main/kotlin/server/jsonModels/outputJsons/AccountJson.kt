package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable


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