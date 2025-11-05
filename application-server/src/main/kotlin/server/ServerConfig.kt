package server


import aiAgent.scripts.AgentCreation
import database.DatabaseConfig
import database.QueryManager
import database.tables.TabellaElettrodiTable
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
import scripts.MainScript
import database.tables.TabellaUserTable
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
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import okhttp3.internal.wait
import server.jsonModels.inputJsons.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.AgentJson
import server.jsonModels.inputJsons.RegisterJson
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.AccountJson
import server.jsonModels.outputJsons.PredictJson
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
            val userId = MainScript.registerUser(credentials)
            call.response.header("Location", "/login")
            call.respondText(AccountJson(userId).toString())
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

        //questo get serve a dare l'indirizzo all'applicazione per prendere i grafici
        get("/graph/{id}/{name}") { //http://0.0.0.0:8080/graph/1/stress 
            val name = call.parameters["name"]
            val userId = call.parameters["id"]
            val filename = when (name) {
                "stress"   -> "grafico_stress_finale_barre.png"
                "activity" -> "grafico_attivita_finale.png"
                "sleep"    -> "grafico_sonno_finale.png"
                else -> return@get call.respondText("Graph not found")
            }
            //luogo sulla macchina fisico dove stanno i grafici generati
            val file = File("/home/ubuntu/GraphGeneratorLogic/graphs/$userId/$filename")
            // il dollaro sta per la f string quindi & e poi nome variabile
            if (!file.exists()) return@get call.respondText("Graph $filename for user $userId not valid")
            call.respondFile(file)
        }

        get ("/generate/{id}") {
            val userId = call.parameters["id"].toString()
            val scriptPath = "/home/ubuntu/GraphGeneratorLogic/graph_generator.py"
            //è il percorso dove vai prendere i file pyhton da avviare
            try {
                val processBuilder = ProcessBuilder( //avvia il mio file python "python3 graph_generator.py id"
                    "/usr/bin/python3",
                    scriptPath, // se servissero dei parametri dovrei scriverli qui sotto
                    userId
                )
                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                process.waitFor(60, TimeUnit.SECONDS)
                if (process.exitValue() == 0) {
                    call.respondText("return success\n")
                } else {
                    call.respondText(
                        "Errore durante l'esecuzione: $output",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            } catch (e: Exception) {
                call.respondText("Train Failed: ${e.stackTraceToString()}")
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
