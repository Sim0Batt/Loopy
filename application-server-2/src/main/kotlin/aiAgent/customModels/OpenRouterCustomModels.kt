package aiAgent.customModels

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
    val LLama4Maverick: LLModel = LLModel(
        provider = LLMProvider.OpenRouter,
        id = "meta-llama/llama-4-maverick:free",
        capabilities = standardCapabilities,
        contextLength = 163_800,
        maxOutputTokens = 163_800,
    )

    val Gemini20Experimental: LLModel = LLModel(
        provider = LLMProvider.OpenRouter,
        id = "google/gemini-2.0-flash-exp:free",
        capabilities = standardCapabilities,
        contextLength = 1_048_576,
        maxOutputTokens = 1_048_576,
    )
}