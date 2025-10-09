package server


import aiAgent.scripts.AgentCreation
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import server.models.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.models.AgentJson
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

        get("/getPPG/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaPpgTable.selectAll().filter {
                    it[TabellaPpgTable.userId] == id?.toInt()
                }.joinToString { "HeartRate:${it[TabellaPpgTable.battito]}, Oxygenation:${it[TabellaPpgTable.ossigenazione]}, TimeStamp:${it[TabellaPpgTable.timestamp]}\n" }
            }
            call.respondText(info)
        }

        get("/getAccelerometer/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaAccelerometroTable.selectAll().filter {
                    it[TabellaAccelerometroTable.userId] == id?.toInt()
                }.joinToString { "Mooving:${it[TabellaAccelerometroTable.movimento]}, TimeStamp:${it[TabellaAccelerometroTable.timestamp]}\n" }
            }
            call.respondText(info)
        }

        get("/getElectrodes/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaElettrodiTable.selectAll().filter {
                    it[TabellaElettrodiTable.userId] == id?.toInt()
                }.joinToString { "Sweating:${it[TabellaElettrodiTable.sudorazione]}, TimeStamp:${it[TabellaElettrodiTable.timestamp]}\n" }
            }
            call.respondText(info)
        }

        get("/getTermometer/{id}"){
            val id = call.parameters["id"]
            val info = transaction(DatabaseConfig().getConfig()) {
                TabellaTermometroTable.selectAll().filter {
                    it[TabellaTermometroTable.userId] == id?.toInt()
                }.joinToString { "Temperature:${it[TabellaTermometroTable.temperatura]}, TimeStamp:${it[TabellaTermometroTable.timestamp]}\n" }
            }
            call.respondText(info)
        }

        post("/agentProcess") {
            val credentials = call.receive<AgentJson>()
            var id = 0
            var age = 0
            var height = 0
            var weight = 0
            var sex = ""
            println(credentials)

            transaction(DatabaseConfig().getConfig()) {
                id = TabellaUserTable.selectAll().where{
                    TabellaUserTable.username eq credentials.username
                }.firstOrNull()?.get(TabellaUserTable.id).toString().toInt()

                age = TabellaUserTable.select(TabellaUserTable.age).where{
                    TabellaUserTable.id eq id
                }.firstOrNull()?.get(TabellaUserTable.age).toString().toInt()

                height = TabellaUserTable.selectAll().where{
                    TabellaUserTable.id eq id
                }.firstOrNull()?.get(TabellaUserTable.height).toString().toInt()

                weight = TabellaUserTable.selectAll().where{
                    TabellaUserTable.id eq id
                }.firstOrNull()?.get(TabellaUserTable.weight).toString().toInt()

                sex = TabellaUserTable.selectAll().where{
                    TabellaUserTable.id eq id
                }.firstOrNull()?.get(TabellaUserTable.sex).toString()

            }

            val agent = AgentCreation().getAgent(id)
            call.respondText(agent.run("""
                User input: ${credentials.input}
                Context:
                -UserId: $id,
                -Age: $age,
                -Height: $height,
                -Weight: $weight,
                -Sex: $sex
            """.trimIndent()))

            call.respondText(agent.run(credentials.input))
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
