package server


import aiAgent.scripts.AgentCreation
import database.DatabaseConfig
import database.QueryManager
import database.tables.TabellaSensorsStatusTable
import org.jetbrains.exposed.sql.SortOrder
import scripts.MainScript
import database.tables.TabellaUserTable
import graph.GraphsManagement
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
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import server.jsonModels.inputJsons.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.AgentJson
import server.jsonModels.inputJsons.RegisterJson
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.AccountJson
import server.jsonModels.outputJsons.PredictJson
import server.jsonModels.outputJsons.StatusJson
import server.jsonModels.outputJsons.UserDataJson
import java.io.File
import java.util.concurrent.TimeUnit


fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }


    routing {
        //LOGIN LOGIC
        staticResources("static", "static")
        get("/getUsers") {
            call.respondText {
                MainScript.getUsers()
            }
        }

        post("/login") {
            val credentials = call.receive<UserJson>()
            var userId = -1
            var username = ""
            transaction(DatabaseConfig.getConfig()) {
                userId = TabellaUserTable.selectAll()
                    .firstOrNull {
                        it[TabellaUserTable.email] == credentials.email &&
                                it[TabellaUserTable.password] == credentials.password
                    }?.getOrNull(TabellaUserTable.id).toString().toInt()

                username = TabellaUserTable.selectAll()
                    .firstOrNull {
                        it[TabellaUserTable.email] == credentials.email &&
                                it[TabellaUserTable.password] == credentials.password
                    }?.getOrNull(TabellaUserTable.username).toString()

            }

            if (userId != -1) {
                call.respondText(AccountJson(userId, username).toString())
            } else {
                call.respondText("failure")
            }
        }

        post("/register") {
            val credentials = call.receive<RegisterJson>()
            val userId = MainScript.registerUser(credentials)
            var username = ""
            transaction(DatabaseConfig.getConfig()) {
                username = TabellaUserTable.selectAll().where{
                    TabellaUserTable.id eq userId
                }.firstOrNull()?.get(TabellaUserTable.username).toString()
            }

            call.response.header("Location", "/login")
            call.respondText(AccountJson(userId, username).toString())
        }

        post("/user/{id}") {
            val userId = call.parameters["id"]?.toInt()
            var userJson = UserDataJson("", "", "", "", "", "")

            transaction(DatabaseConfig.getConfig()) {
                userJson = QueryManager.getUserInformation(userId!!)
            }
            call.respondText(userJson.toString())
        }


        //DATA LOGIC
        staticResources("static", "static")
        post("/saveData/{id}") {
            val userId = call.parameters["id"]
            val input = call.receive<SaveDataJson>()
            println{"Received: \n${input}"}
            call.response.header("Content-Type", "application/json")
            QueryManager.saveDatas(DatabaseConfig.getConfig(), input, userId.toString().toInt())
            call.respondText("Data Saved")
        }

        get("/getDatas/{id}") {
            val id = call.parameters["id"]
            call.response.header("Content-Type", "application/json")
            call.respondText(QueryManager.getDatas(DatabaseConfig.getConfig(), id.toString().toInt()).toString())
        }

        // Chiamata per prendere il Json con i dati "complessi"
        get("/getSummary/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respondText("Missing id", status = HttpStatusCode.BadRequest)
                return@get
            }
            call.response.header("Content-Type", "application/json")

            val riepilogo = QueryManager.getDailySummary(DatabaseConfig.getConfig(), id.toString().toInt())

            call.respond(riepilogo) // se te lo stai chiedendo, non ho messo respondText perche respond lo fa in automatico ed è piu efficiente
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
                """.trimIndent()
                )
            )
        }


        get ("/trainModel"){
            val scriptPath = "/home/ubuntu/MLLoopy/train.py"
            try{
                println("Process Started")
                val processBuilder = ProcessBuilder( // /usr/bin/python3 /home/ubuntu/MLLoopy/train.py 1
                    "/usr/bin/python3",
                    scriptPath,
                )

                val process = processBuilder.start()

                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()

                process.waitFor(60, TimeUnit.SECONDS)
                if (process.exitValue() == 0) {
                    call.respondText("success")
                } else {
                    call.respondText("Errore durante l'esecuzione: $output", status = HttpStatusCode.InternalServerError)
                }

            }catch (e: Exception){
                call.respondText("Train Failed: ${e.stackTraceToString()}")
            }
        }

        get("/modelProcess/{id}") {
            val userId = call.parameters["id"].toString().toInt()
            try{
                MainScript.generateCsv(userId.toString().toInt())

                println("Process Started")

                Thread {
                    MainScript.executePythonScript(userId.toString())
                }.start()

                println("Waiting 10 seconds...")
                Thread.sleep(10000)
                val predict = QueryManager.getGlucosePredict(userId)

                call.respondText(PredictJson(userId, predict).toString())
            }catch (e: Exception){
                    call.respondText("Process Failed: ${e.stackTraceToString()}")
            }
        }

        //Sensors Staus
        get("/status/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            var currentStatus: ResultRow? = null
            transaction(DatabaseConfig.getConfig()) {
                currentStatus = TabellaSensorsStatusTable.selectAll().where{
                    TabellaSensorsStatusTable.userId eq userId
                }.orderBy(TabellaSensorsStatusTable.id to SortOrder.DESC).firstOrNull()
            }
            call.respondText(StatusJson(
                currentStatus?.get(TabellaSensorsStatusTable.accelerometerStatus).toString(),
                currentStatus?.get(TabellaSensorsStatusTable.thermometerStatus).toString(),
                currentStatus?.get(TabellaSensorsStatusTable.ppgStatus).toString(),
                currentStatus?.get(TabellaSensorsStatusTable.electrodeStatus).toString(),
                currentStatus?.get(TabellaSensorsStatusTable.timestamp).toString()
                ).toString()
            )
        }

        post("/saveStatus/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            val statusJson = call.receive<StatusJson>()
            transaction(DatabaseConfig.getConfig()) {
                TabellaSensorsStatusTable.insert {
                    it[accelerometerStatus] = statusJson.accelerometerStatus
                    it[ppgStatus] = statusJson.ppgStatus
                    it[thermometerStatus] = statusJson.thermometerStatus
                    it[electrodeStatus] = statusJson.electrodesStatus
                    it[timestamp] = statusJson.timestamp
                    it[TabellaSensorsStatusTable.userId] = userId
                }
            }
        }

        get("/generateGraph/{graphType}/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            val graphType = call.parameters["graphType"]
            val path = "/home/ubuntu/GraphGeneratorLogic/graphs/$userId"

            File(path).mkdirs()

            when(graphType){
                "stress" -> {
                    GraphsManagement.generateStressGraph(userId, path)
                    call.respondFile(File("$path/stress_graph.png"))
                }
                "activity" -> {
                    GraphsManagement.generateActivityGraph(userId, path)

                    call.respondFile(File("$path/activity_graph.png"))
                }
                "sleep" -> {
                    GraphsManagement.generateSleepGraph(userId, path)
                    call.respondFile(File("$path/sleep_graph.png"))
                }
                else -> call.respondText("Invalid Graph Type")
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
