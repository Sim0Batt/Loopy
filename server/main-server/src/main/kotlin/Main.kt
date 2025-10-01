import config.ServerConfig

enum class Argument {SERVER, HELP, TEST}
internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        val arg = when{
            args.contains("-h") || args.contains("help") -> Argument.HELP
            args.contains("-s") || args.contains("server") -> Argument.SERVER
            args.contains("-t") || args.contains("test") -> Argument.TEST
            else -> Argument.SERVER
        }

        when(arg){
            Argument.HELP -> println("Help")
            Argument.SERVER -> ServerConfig().run()
            Argument.TEST -> println("Test")
        }
    }
}