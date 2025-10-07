import scripts.MainScript

internal object Main{
    @JvmStatic
    suspend fun main(args: Array<String>) {
        MainScript().runAgent(args[0])
    }
}