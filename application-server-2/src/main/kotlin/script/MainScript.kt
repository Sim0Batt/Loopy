package org.example.script

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import server.inputJsons.CsvDataJson
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

object MainScript {

    suspend fun generateCsv(userId: Int){
        val csvPath = "/home/ubuntu/MLLoopy/csvs/result$userId.csv"
        val outputFile = File(csvPath)

        val data = client.get("http://0.0.0.0:18034/data/csv/$userId").body<CsvDataJson>()
        try{
            outputFile.bufferedWriter().use { writer ->
                writer.write("eda,hr,temp\n")
            }
            for(i in 0..<data.hrs.split(",").size){
                outputFile.appendText("${data.sweatings.split(",")[i]},${data.hrs.split(",")[i]},${data.temperatures.split(",")[i]}\n")
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