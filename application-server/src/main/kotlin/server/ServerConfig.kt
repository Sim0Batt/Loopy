package server


import aiAgent.scripts.AgentCreation
import database.DatabaseConfig
import database.QueryManager
import database.tables.TabellaGlucosioTable
import scripts.MainScript
import database.tables.TabellaUserTable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import server.jsonModels.inputJsons.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.AgentJson
import server.jsonModels.inputJsons.RegisterJson
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.AccountJson
import kotlinx.serialization.json.Json as KotlinxJson
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }





    routing {
        //LOGIN LOGIC
        staticResources("static", "static")
        get("/getUsers") {
            call.respondText {
                MainScript().getUsers()
            }
        }

        post("/login") {
            val credentials = call.receive<UserJson>()

            val userId = transaction(DatabaseConfig.getConfig()) {
                TabellaUserTable.selectAll()
                    .firstOrNull {
                        it[TabellaUserTable.email] == credentials.email &&
                                it[TabellaUserTable.password] == credentials.password
                    }?.getOrNull(TabellaUserTable.id).toString().toIntOrNull()
            }

            if (userId != null) {
                call.respondText(AccountJson(userId).toString())
            } else {
                call.respondText("failure")
            }
        }

        post("/register") {
            val credentials = call.receive<RegisterJson>()
            val userId = MainScript().registerUser(credentials)
            call.response.header("Location", "/login")
            call.respondText(AccountJson(userId).toString())
        }


        //DATA LOGIC
        staticResources("static", "static")
        post("/saveData") {
            val input = call.receive<SaveDataJson>()
            println{"Received: \n${input}"}
            call.response.header("Content-Type", "application/json")
            QueryManager.saveDatas(DatabaseConfig.getConfig(), input)
            call.respondText("Data Saved")
        }

        get("/getDatas/{id}") {
            val id = call.parameters["id"]
            call.response.header("Content-Type", "application/json")
            call.respondText(QueryManager.getDatas(DatabaseConfig.getConfig(), id.toString().toInt()).toString())
        }


        //AI AGENT LOGIC
        post("/agentProcess") {
            val credentials = call.receive<AgentJson>()
            var id = 0
            var age = 0
            var height = 0
            var weight = 0
            var sex = ""
            println(credentials)

            transaction(DatabaseConfig.getConfig()) {
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


        //MACHINE LEARNING MODELS LOGIC
        get("/modelProcess/{id}") {
            try{
                val userId = call.parameters["id"]
                val client = HttpClient(CIO) {
                    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                            json(KotlinxJson {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            })
                        }
                    }

                    val response = client.get ("http://0.0.0.0:18034/process/${userId.toString()}")
                    // Log della risposta del server
                    val responseBody = response.bodyAsText()
                    println("Risposta del server: $responseBody")
                    QueryManager.saveGlucosePrediction(
                        DatabaseConfig.getConfig(),
                        responseBody.toDouble(),
                        userId.toString().toInt()
                    )

                    call.respondText("Process Successful")
                }catch (e: Exception){
                    call.respondText("Process Failed: ${e}")
                }
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
