package server


import database.DatabaseConfig
import database.dao.TabellaUserEntity
import database.tables.TabellaUserTable
import io.ktor.http.HttpStatusCode
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
import org.example.main.server.models.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    routing {
        staticResources("static", "static")
        get("/getUsers") {
            var users = ""
            transaction(DatabaseConfig().getConfig()) {
                TabellaUserTable.selectAll().map {
                    users += it[TabellaUserTable.id].toString() + ", " + it[TabellaUserTable.email] + ", " + it[TabellaUserTable.password] + "\n"
                }
            }
            call.respondText {
                users
            }
        }

        post("/login") {
            val credentials = call.receive<UserJson>()

            val user = transaction(DatabaseConfig().getConfig()) {
                TabellaUserTable.selectAll()
                    .firstOrNull {
                        it[TabellaUserTable.email] == credentials.email &&
                                it[TabellaUserTable.password] == credentials.password
                    }
            }

            if (user != null) {
                call.respondText("success")
            } else {
                call.respondText("failure")
            }
        }
    }
}

class ServerConfig {
    fun run(args: Array<String> = emptyArray()) {
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}
