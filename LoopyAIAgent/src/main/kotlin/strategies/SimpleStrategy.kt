package strategies
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.memory.config.MemoryScopeType
import ai.koog.agents.memory.feature.nodes.nodeSaveToMemory
import ai.koog.agents.memory.feature.nodes.nodeLoadFromMemory
import ai.koog.agents.memory.model.Concept
import ai.koog.agents.memory.model.FactType
import ai.koog.agents.memory.model.MemorySubject

/**
 * Creates a strategy for the tone analysis agent with memory.
 */
fun SimpleStrategy(name: String): AIAgentStrategy<String, String> {
    return strategy(name) {
        val nodeSendInput by nodeLLMRequest()
        val nodeExecuteTool by nodeExecuteTool()
        val nodeSendToolResult by nodeLLMSendToolResult()

        // Memory nodes
        val nodeSaveQuestion by nodeSaveToMemory<String>(
            concept = Concept(
                keyword = "question",
                description = "User's previous question",
                factType = FactType.MULTIPLE
            ),
            subject = MemorySubject.Everything,
            scope = MemoryScopeType.PRODUCT
        )
        val nodeLoadQuestions by nodeLoadFromMemory<String>(
            concepts = listOf(
                Concept(
                    keyword = "question",
                    description = "User's previous question",
                    factType = FactType.MULTIPLE
                )
            ),
            subject = MemorySubject.Everything,
            scope = MemoryScopeType.PRODUCT

        )


        // Load previous questions from memory at the start
        edge(nodeStart forwardTo nodeLoadQuestions)

        // After loading, continue to LLM input
        edge(nodeLoadQuestions forwardTo nodeSendInput)

        // If the LLM responds with a message, finish and save the question to memory
        edge((nodeSendInput forwardTo nodeSaveQuestion) onAssistantMessage { true })
        edge(nodeSaveQuestion forwardTo nodeFinish)

        // If the LLM calls a tool, execute it
        edge((nodeSendInput forwardTo nodeExecuteTool) onToolCall { true })

        // After tool usage
        edge(nodeExecuteTool forwardTo nodeSendToolResult)

        // If the LLM calls another tool, execute it
        edge((nodeSendToolResult forwardTo nodeExecuteTool) onToolCall { true })

        // After second tool usage
        edge(nodeExecuteTool forwardTo nodeSendToolResult)

        // If the LLM responds with a message, finish and save the question to memory
        edge((nodeSendToolResult forwardTo nodeSaveQuestion) onAssistantMessage { true })

        //End saving hte question
        edge(nodeSaveQuestion forwardTo nodeFinish)
    }
}