package tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

@LLMDescription("""
    Prendi tutti i dati dell'utente e utilizzali per dare consigli.
""")
class GetStatusTool: ToolSet {
    @Tool
    fun getHeartInformations(): String{
        return "HeartRates"
    }

}