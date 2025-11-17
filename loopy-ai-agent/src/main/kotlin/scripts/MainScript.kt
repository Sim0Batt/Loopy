package scripts

class MainScript {
    suspend fun runAgent(input: String){
        val agent = AgentCreation().getOpenRouterAgent()
        agent.run(input)
    }
}