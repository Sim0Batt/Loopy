import server.ServerConfig

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        ServerConfig().run()
    }
}