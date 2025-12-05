package tools

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
class GetStatusTool : ToolSet {
    @Tool
    @LLMDescription("""Using this tool, you can retrieve information about the user's heart rate from an external server. 
        Hence, when the user requests data about their heart rate, use this tool to provide them with the information.
        The data returned by the server is a string: HeartRate:val, Oxygenation:val, TimeStamp:val.
        
    """)
    suspend fun getHeartInformations(
        @LLMDescription("The user's ID")
        id: Int
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
            val response = client.get("http://51.20.84.226:8080/getPPG/$id")
            responseBody = response.bodyAsText()
            println("Response by server: $response")
            println("Server response: $responseBody")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return responseBody
    }


    @Tool
    @LLMDescription("""Using this tool, you can retrieve information about the user's sweating during the day from an external server. 
        Hence, when the user requests data about their sweating or water informations use this tool to provide them with the information.
        The data returned by the server is a string: Sweating:val, TimeStamp:val.
        
    """)
    suspend fun getElectrodeInformations(
        @LLMDescription("The user's ID")
        id: Int
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
            val response = client.get("http://51.20.84.226:8080/getElectrodes/$id")
            responseBody = response.bodyAsText()
            println("Response by server: $response")
            println("Server response: $responseBody")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return responseBody
    }


    @Tool
    @LLMDescription("""Using this tool, you can retrieve information about the user's training during the day from an external server.
        The accelerometer returns the informations only if the user is moving (0 or 1) on a specific time catched every 5 seconds.
        Hence, when the user requests data about their training informations, use this tool to provide them with the information.
        The data returned by the server is a string: Mooving:val, TimeStamp:val.
        
    """)
    suspend fun getAccelerometerInformations(
        @LLMDescription("The user's ID")
        id: Int
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
            val response = client.get("http://51.20.84.226:8080/getAccelerometer/$id")
            responseBody = response.bodyAsText()
            println("Response by server: $response")
            println("Server response: $responseBody")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return responseBody
    }

    @Tool
    @LLMDescription("""Using this tool, you can retrieve information about the user's temperature during the day from an external server.
        Hence, when the user requests data about their temperature, use this tool to provide them with the information.
        The data returned by the server is a string: Mooving:val, TimeStamp:val.
        
    """)
    suspend fun getTemperatureInformations(
        @LLMDescription("The user's ID")
        id: Int
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
            val response = client.get("http://51.20.84.226:8080/getTermometer/$id")
            responseBody = response.bodyAsText()
            println("Response by server: $response")
            println("Server response: $responseBody")
        } catch (e: Exception) {
            println("Error during Heart Rate request")

        }
        return responseBody
    }
}