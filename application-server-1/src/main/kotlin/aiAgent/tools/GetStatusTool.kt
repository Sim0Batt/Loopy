package aiAgent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import database.DatabaseConfig
import database.QueryManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import server.jsonModels.outputJsons.ReturnDataJson

@LLMDescription(
    """
    Retrieve all user data and use them to give advice.
"""
)
class GetStatusTool(val id: Int) : ToolSet {
    @Tool
    @LLMDescription("""Using this tool, you can retrieve all the status informations about the user in a way:
{
    "heartRate": "[...]",
    "oxygen": "[...]",
    "timestampPPG": "[...]",
    "sweating": "[...]",
    "timestampElectrodes": "[...]",
    "temperature": "[...]",
    "timestampTermometer": "[...]"
    "movement": "[...]"
    "timestampAccelerometer": "[...]"
}. 
Inside the [...] you will find a list of the relative last 10 values saved. Respond at the user question using these datas.
    """)
    fun getHealthInformation(
    ): String {
        var userData = ""
        try {
            userData = QueryManager.getDatas(DatabaseConfig.getConfig(), id).toString()
            println("Data saved for user $id: $userData")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return userData
    }
}