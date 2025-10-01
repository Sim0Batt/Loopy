package config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.header
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    routing {
        staticResources("static", "static")
        post("/process") {
            call.response.header("Content-Type", "application/json")
        }
    }
}

class ServerConfig {
    fun run(args: Array<String> = emptyArray()) {
        embeddedServer(
            Netty,
            port = 8080,
            host = "127.0.0.1",
            module = Application::module
        ).start(wait = true)
    }
}
