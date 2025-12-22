package server


import aiAgent.scripts.AgentCreation
import database.QueryManager
import graph.GraphsManagement
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.script.MainScript
import org.example.server.inputJsons.UserDataJson
import server.jsonModels.inputJsons.AgentJson
import server.jsonModels.outputJsons.PredictJson
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json as KotlinxJson



val client = HttpClient (CIO) {
    install(ContentNegotiation) {
        json(KotlinxJson {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}


fun Application.module() {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }




    routing {
        //AI LOGIC
        post("/agentProcess/{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()
            val credentials = call.receive<AgentJson>()
            println(credentials)


            val userData = client.post("http://13.60.104.145:8080/user/$userId").body<UserDataJson>()



            val agent = AgentCreation().getAgent(userId!!)
            call.respondText(agent.run("""
                User input: ${credentials.input}
                Context:
                -Username: ${userData.username},
                -Age: ${userData.age},
                -Height: ${userData.height},
                -Weight: ${userData.weight},
                -Sex: ${userData.gender}
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

        get("/getSSAGData/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            try {
                call.respond(QueryManager.getSSGAData(userId))
            }catch (e: Exception){
                call.respondText("Error during SSAG request: ${e.stackTraceToString()}")
            }
        }


        //Graphs Logic
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
