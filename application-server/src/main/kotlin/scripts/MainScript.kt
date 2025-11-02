package scripts

import database.DatabaseConfig
import database.tables.TabellaElettrodiTable
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.RegisterJson
import java.io.File
import java.util.concurrent.TimeUnit

object MainScript {
    fun getUsers(): String{
        var users = ""
        transaction(DatabaseConfig.getConfig()) {
            TabellaUserTable.selectAll().map {
                users += it[TabellaUserTable.id].toString() + ", " + it[TabellaUserTable.email] + ", " +
                        it[TabellaUserTable.password] + ", " +
                        it[TabellaUserTable.username] + ", " +
                        it[TabellaUserTable.age] + ", " +
                        it[TabellaUserTable.height] + ", " +
                        it[TabellaUserTable.weight] + "\n"

            }
        }
        return users
    }

    fun registerUser(credentials: RegisterJson): Int{
        var userId = 0
        transaction(DatabaseConfig.getConfig()) {
            TabellaUserTable.insert {
                it[email] = credentials.email
                it[password] = credentials.password
                it[username] = credentials.username
                it[age] = credentials.age
                it[height] = credentials.height
                it[weight] = credentials.weight
                it[sex] = credentials.sex
            }

            userId = TabellaUserTable.selectAll().where {
                TabellaUserTable.email eq credentials.email
            }.firstOrNull()?.getOrNull(TabellaUserTable.id).toString().toIntOrNull() ?: throw Exception("User not registered")
        }
        return userId
    }

    fun generateCsv(userId: Int){
        val csvPath = "/home/ubuntu/MLLoopy/csvs/result$userId.csv"
        val outputFile = File(csvPath)
        var sweatings = listOf<Double>()
        var hrs = listOf<Int>()
        var temperatures = listOf<Double>()

        transaction(DatabaseConfig.getConfig()) {
            sweatings = TabellaElettrodiTable.selectAll().where {
                TabellaElettrodiTable.userId eq userId
            }.map { it[TabellaElettrodiTable.sudorazione] }.toList().takeLast(10)

            hrs = TabellaPpgTable.selectAll().where {
                    TabellaPpgTable.userId eq userId
            }.map { it[TabellaPpgTable.battito] }.toList().takeLast(10)

            temperatures = TabellaTermometroTable.selectAll().where {
                    TabellaTermometroTable.userId eq userId
            }.map { it[TabellaTermometroTable.temperatura] }.toList().takeLast(10)
        }
        try{
            outputFile.bufferedWriter().use { writer ->
                writer.write("eda,hr,temp\n")
            }
            for(i in 0..(hrs.size-1)){
                outputFile.appendText("${sweatings[i]},${hrs[i]},${temperatures[i]}\n")
            }
        }catch (e: Exception){
            println("Error during CSV generation: ${e.stackTraceToString()}")
        }
    }

    fun executePythonScript(userId: String) {
        var process: Process? = null
        try {
            val scriptPath = "/home/ubuntu/MLLoopy/predict.py"
            val processBuilder = ProcessBuilder(
                "/usr/bin/python3",
                scriptPath,
                userId,
            )

            processBuilder.redirectErrorStream(false)

            println("Starting Python for user $userId")
            process = processBuilder.start()

            // Timeout di 30 secondi
            val completed = process.waitFor(30, TimeUnit.SECONDS)


        } catch (e: Exception) {
            println("Python execution failed for user $userId: ${e.message}")
            e.printStackTrace()
        } finally {
            process?.destroy()
        }
    }
}