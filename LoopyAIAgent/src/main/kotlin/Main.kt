import kotlinx.coroutines.runBlocking
import scripts.MainScript

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // val input = readln()
            println(MainScript().runAgent("""
                Input: what was my average hearth beat?
                Context:
                - You are the user with id 1
                """.trimIndent())
            )
        }
    }
}