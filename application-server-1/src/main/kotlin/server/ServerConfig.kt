package server


import database.DatabaseConfig
import database.QueryManager
import database.tables.TabellaSensorsStatusTable
import org.jetbrains.exposed.sql.SortOrder
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
import io.ktor.server.response.respondText
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import server.jsonModels.inputJsons.UserJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.RegisterJson
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.AccountJson
import server.jsonModels.outputJsons.StatusJson
import server.jsonModels.outputJsons.UserDataJson


fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            allowSpecialFloatingPointValues = true
        })
    }


    routing {
        //USERS LOGIC
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

        post("/editUser/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            val editJson = call.receive<RegisterJson>()

            val isLoaded = QueryManager.updateUserInformation(userId, editJson)
            if(isLoaded) call.respondText("User Updated") else call.respondText("Error during update", status = HttpStatusCode.InternalServerError)
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
            println("Response: ${QueryManager.getDatas(DatabaseConfig.getConfig(), id.toString().toInt())}")
            call.respond(QueryManager.getDatas(DatabaseConfig.getConfig(), id.toString().toInt()))
        }

        get("/data/csv/{id}"){
            val userId = call.parameters["id"].toString().toInt()
            val response = QueryManager.getCsvData(userId)
            if(response.hrs.isNotEmpty() && response.sweatings.isNotEmpty() && response.temperatures.isNotEmpty()) {
                println(response.toString())
                call.respond(response)
            } else {
                println(response.toString())
                call.respondText("No data available", status = HttpStatusCode.NotFound)
            }
        }

        //Sensors Staus Logic
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
