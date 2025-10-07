package customModels

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

private val standardCapabilities: List<LLMCapability> = listOf(
    LLMCapability.Temperature,
    LLMCapability.Schema.JSON.Standard,
    LLMCapability.Speculation,
    LLMCapability.Tools,
    LLMCapability.ToolChoice,
    LLMCapability.Completion
)

object OpenRouterCustomModels {
    val DeepSeekV3Free: LLModel = LLModel(
        provider = LLMProvider.OpenRouter,
        id = "deepseek/deepseek-chat-v3.1:free",
        capabilities = standardCapabilities,
        contextLength = 163_800,
        maxOutputTokens = 163_800,
    )
}