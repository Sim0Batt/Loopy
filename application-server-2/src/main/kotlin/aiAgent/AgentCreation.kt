package aiAgent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import aiAgent.strategies.MockStrategy
import database.QueryManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import org.example.server.inputJsons.UserDataJson
import server.inputJsons.ReturnDataJson
import utils.APPLICATION_SERVER_1_IP
import kotlinx.serialization.json.Json as KotlinxJson


object AgentCreation {
    val client = HttpClient (CIO) {
        install(ContentNegotiation) {
            json(KotlinxJson {
                isLenient = true
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
            })
        }
    }
    suspend fun getAgent(id: Int): AIAgent<String, String> {

        val data = client.get("http://${APPLICATION_SERVER_1_IP}:8080/getDatas/$id").body<ReturnDataJson>()

        val user = client.post("http://${APPLICATION_SERVER_1_IP}:8080/user/$id").body<UserDataJson>()

        val eventHandlerConfig: EventHandlerConfig.() -> Unit = {
            onToolCallStarting { toolContext ->
                println("Tool called: tool=${toolContext.tool.name}")
            }

            onToolValidationFailed { toolContext ->
                println("Tool validation error: tool=${toolContext.tool.name}")
            }

            onToolCallFailed { toolContext ->
                println("Tool call failure: tool=${toolContext.tool.name}")
            }

            onToolCallCompleted { toolContext ->
                println("Tool call result: tool=${toolContext.tool.name}")
            }
        }

        return AIAgent(
            promptExecutor = simpleGoogleAIExecutor("AIzaSyDXATpG9D5-7zNoKXszHi0SnleB0hUdKVs"),
            llmModel = GoogleModels.Gemini2_5Flash,
            strategy = MockStrategy(),
            systemPrompt = generatePrompt(user, data, id)
        ) {
            install(EventHandler.Feature) { eventHandlerConfig() }
        }
    }


    private fun generatePrompt(user: UserDataJson, data: ReturnDataJson, userId: Int): String {
        val ssagData = QueryManager.getSSGAData(userId)
        return """
CONTEXT:
---------------------------------------------------------------------------------------------
Your name is Loopy, you are an assistant to help the users when they make you questions on their health.
Respond calmy, if you cannot retrive problems respond "I have problems to retrive your data, please try again later".
---------------------------------------------------------------------------------------------

RULES:
Respond in max 100 words
Don't invent data or tools
Never put into the response information about code or tools or structures recived in the context.
Use simple words without being too much specific. 
---------------------------------------------------------------------------------------------

USER DATA:
---------------------------------------------------------------------------------------------
User Name: ${user.username}
User Age: ${user.age}
User Height: ${user.height}
User Weight: ${user.weight}
---------------------------------------------------------------------------------------------

DAILY DATA:
---------------------------------------------------------------------------------------------
Hearth rate average: ${data.heartRate}
Blood Oxygen average: ${data.oxygen}
Skin humidity average: ${data.sweating}
Temperature average: ${data.temperature}
Stress level average: ${ssagData.stress}
Sleep level average: ${ssagData.sleep}
Activity level average: ${ssagData.activity}
---------------------------------------------------------------------------------------------
""".trimIndent()
    }
}