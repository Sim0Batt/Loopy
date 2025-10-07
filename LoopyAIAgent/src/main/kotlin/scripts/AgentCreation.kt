package scripts

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.eventHandler.feature.EventHandlerConfig
import ai.koog.agents.memory.feature.AgentMemory
import ai.koog.agents.memory.providers.LocalFileMemoryProvider
import ai.koog.agents.memory.providers.LocalMemoryConfig
import ai.koog.agents.memory.storage.SimpleStorage
import ai.koog.agents.snapshot.feature.Persistency
import ai.koog.agents.snapshot.providers.InMemoryPersistencyStorageProvider
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.rag.base.files.JVMFileSystemProvider
import strategies.SimpleStrategy
import configuration.ReadXMLResources
import customModels.OpenRouterCustomModels
import tools.GetStatusTool
import java.nio.file.Path

class AgentCreation {
    val xmlReader = ReadXMLResources()
    val configuration = xmlReader.run()

    fun getOpenRouterAgent(): AIAgent<String, String>{
        val memoryProvider = LocalFileMemoryProvider(
            config = LocalMemoryConfig("Agent-Memory"),
            storage = SimpleStorage(fs = JVMFileSystemProvider.ReadWrite),
            fs = JVMFileSystemProvider.ReadWrite,
            root = Path.of("~/tmp/LoopyAgent/Memory")
        )


        val eventHandlerConfig: EventHandlerConfig.() -> Unit = {
            onToolCall { toolContext ->
                println("Tool called: tool=${toolContext.tool.name}")
            }

            onToolValidationError { toolContext ->
                println("Tool validation error: tool=${toolContext.tool.name}")
            }

            onToolCallFailure { toolContext ->
                println("Tool call failure: tool=${toolContext.tool.name}")
            }

            onToolCallResult { toolContext ->
                println("Tool call result: tool=${toolContext.tool.name}")
            }
        }

        val SYSTEM_PROMPT = """
            
        """.trimIndent()

        return AIAgent(
            toolRegistry = ToolRegistry {
                tools(GetStatusTool().asTools())
            },
            strategy = SimpleStrategy("simple"),
            executor = simpleOpenRouterExecutor(configuration.openRouterKey),
            llmModel = OpenRouterCustomModels.DeepSeekV3Free,
            systemPrompt = SYSTEM_PROMPT,
            installFeatures = {
                install(AgentMemory) {
                    this.memoryProvider = memoryProvider
                    agentName = "Loopy"
                    featureName = "richieste-utente"
                    organizationName = "Loopy"
                    productName = "memory"
                }
                install(EventHandler.Feature, eventHandlerConfig)

                install(Persistency) {
                    storage = InMemoryPersistencyStorageProvider("agent-checkpoints")
                    enableAutomaticPersistency = true
                }
            }
        )
    }
}