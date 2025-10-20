package server

import database.DatabaseConfig
import database.QueryManagement
import io.ktor.http.plus
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import server.inputJsons.SaveDataJson


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }


    routing {
        staticResources("static", "static")
        post("/saveData") {
            val input = call.receive<SaveDataJson>()
            println{"Received: \n${input}"}
            call.response.header("Content-Type", "application/json")
            QueryManagement.saveDatas(DatabaseConfig.getConnection(), input)
            call.respondText("Data Saved")
        }

        get("/getDatas") {
            call.response.header("Content-Type", "application/json")
            call.respondText(QueryManagement.getDatas(DatabaseConfig.getConnection()).toString())
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
