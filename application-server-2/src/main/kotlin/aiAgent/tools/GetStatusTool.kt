package aiAgent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import server.outputJsons.ReturnDataJson
import kotlinx.serialization.json.Json as KotlinxJson

val client = HttpClient (CIO) {
    install(ContentNegotiation) {
        json(KotlinxJson {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

@LLMDescription(
    """
    Retrieve all user data and use them to give advice.
"""
)
class GetStatusTool(val id: Int) : ToolSet {
    @Tool
    @LLMDescription("""Using this tool, you can retrieve all the status information about the user in a way:
{
    "heartRate": "",
    "oxygen": "",
    "sweating": "",
    "temperature": "",
    "glucose": "",
    "activity": "",
    "stress": "",
    "sleep": ""
}. 
All the values are doubles and represents medium values of the current data of today.
    """)
    fun getHealthInformation(
    ): String {
        var userData: ReturnDataJson? = null
        try {
            runBlocking {
                userData = client.get("http://localhost:8080/getDatas/$id").body<ReturnDataJson>()
            }
            println("Data saved for user $id: $userData")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return userData.toString()
    }
}