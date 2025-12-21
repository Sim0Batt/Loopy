package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable

@Serializable
class CsvDataJson(
    val hrs: String,
    val sweatings: String,
    val temperatures: String
) {
    override fun toString(): String {
        return """
{
    hrs: "$hrs",
    sweatings: "$sweatings",
    temperatures: "$temperatures"
}
        """.trimIndent()
    }
}