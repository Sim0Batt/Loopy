package server


import database.DatabaseConfig
import database.scripts.MainScript
import database.tables.TabellaAccelerometroTable
import database.tables.TabellaElettrodiTable
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
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
import server.models.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.models.RegisterJson


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    routing {
        staticResources("static", "static")
        get("/getUsers") {
            call.respondText {
                MainScript().getUsers()
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

        post("/register") {
            val credentials = call.receive<RegisterJson>()
            MainScript().registerUser(credentials)
            call.response.header("Location", "/login")
            call.respondText("Account Created", status = HttpStatusCode.Created)
        }

        post("/getPPG/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaPpgTable.selectAll().filter {
                    it[TabellaPpgTable.userId] == id?.toInt()
                }.joinToString { "Battito: ${it[TabellaPpgTable.battito]}, Ossigenazione: ${it[TabellaPpgTable.ossigenazione]}, TimeStamp: ${it[TabellaPpgTable.timestamp]}" }
            }
            call.respondText(info)
        }

        post("/getAccellerometro/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaAccelerometroTable.selectAll().filter {
                    it[TabellaAccelerometroTable.userId] == id?.toInt()
                }.joinToString { "Movimento: ${it[TabellaAccelerometroTable.movimento]}, TimeStamp: ${it[TabellaAccelerometroTable.timestamp]}" }
            }
            call.respondText(info)
        }

        post("/getElettrodi/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaElettrodiTable.selectAll().filter {
                    it[TabellaElettrodiTable.userId] == id?.toInt()
                }.joinToString { "Sudorazione: ${it[TabellaElettrodiTable.sudorazione]}, TimeStamp: ${it[TabellaElettrodiTable.timestamp]}" }
            }
            call.respondText(info)
        }

        post("/getTermometro/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaTermometroTable.selectAll().filter {
                    it[TabellaTermometroTable.userId] == id?.toInt()
                }.joinToString { "Temperatura: ${it[TabellaTermometroTable.temperatura]}, TimeStamp: ${it[TabellaTermometroTable.timestamp]}" }
            }
            call.respondText(info)
        }
    }
}

class ServerConfig {
    fun run() {
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}
