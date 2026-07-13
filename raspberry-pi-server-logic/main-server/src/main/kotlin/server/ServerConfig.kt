package server

import database.DatabaseConfig
import database.QueryManagement
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import scripts.MainScript
import server.inputJsons.SaveDataJson
import server.outputJsons.StatusJson
import java.time.LocalDateTime
import kotlinx.serialization.json.Json as KotlinxJson
import io.ktor.http.contentType


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

    install(CORS) {
        anyHost()
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

        get("/status"){
            val completed = MainScript.executeAllSensors()
            if(completed == "success"){
                call.respondText("SUCCESS")
            }else{
                call.respondText("FAILURE")
            }
        }

        get("/thermometer"){
            val completed = MainScript.executeThermometerSensor()
            if(completed == "success"){
                call.respondText("SUCCESS")
            }else{
                call.respondText("FAILURE")
            }
        }

        get("/ppg"){
            val completed = MainScript.executePPGSensor()
            if(completed == "success"){
                call.respondText("SUCCESS")
            }else{
                call.respondText("FAILURE")
            }
        }

        get("/electrodes"){
            val completed = MainScript.executeElectrodeSensor()
            if(completed == "success"){
                call.respondText("SUCCESS")
            }else{
                call.respondText("FAILURE")
            }
        }

        get("/accelerometer"){
            val completed = MainScript.executeAccelerometerSensor()
            if(completed == "success"){
                call.respondText("SUCCESS")
            }else{
                call.respondText("FAILURE")
            }
        }

        get ("/saveStatus") {
            val mainStatus = MainScript.executeAllSensors()
            var resultStatusJson: StatusJson
            if(mainStatus == "success"){
                resultStatusJson = StatusJson(
                    "OK",
                    "OK",
                    "OK",
                    "OK",
                    LocalDateTime.now().toString(),
                )
            }else{
                resultStatusJson = StatusJson(
                    if(MainScript.executeAccelerometerSensor() == "success") "OK" else "NA",
                    if(MainScript.executeThermometerSensor() == "success") "OK" else "NA",
                    if(MainScript.executePPGSensor() == "success") "OK" else "NA",
                    if(MainScript.executeElectrodeSensor() == "success") "OK" else "NA",
                    LocalDateTime.now().toString(),
                )
            }

            client.post("http://192.168.1.12:8080/saveStatus/1"){
                contentType(ContentType.Application.Json)
                setBody(resultStatusJson)
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
