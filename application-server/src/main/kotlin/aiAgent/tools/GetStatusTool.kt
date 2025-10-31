package aiAgent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@LLMDescription(
    """
    Retrieve all user data and use them to give advice.
"""
)
class GetStatusTool(val id: Int) : ToolSet {
    @Tool
    @LLMDescription("""Using this tool, you can retrieve all the status informations about the user in a way:
{
    "heartRate": "[]",
    "oxygen": "[...]",
    "timestampPPG": "[....]",
    "sweating": "[...]",
    "timestampElectrodes": "[...]",
    "temperature": "[...]",
    "timestampTermometer": "[...]"
    "movement": "[...]"
    "timestampAccelerometer": "[...]"
}. 
Inside the [...] you will find a list of the relative last 10 values saved. Respond at the user question using these datas.
    """)
    suspend fun getHealthInformation(
    ): String {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }


        var responseBody = ""

        try {
            val response = client.get("http://13.48.59.215:8080/getDatas/$id")
            responseBody = response.bodyAsText()
            println("Response by server: $response")
            println("Server response: $responseBody")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return responseBody
    }
}