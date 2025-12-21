package com.example.loopy.chat.scripts

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ChatCaller {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 40000 // 15 seconds
            connectTimeoutMillis = 40000 // 15 seconds
            socketTimeoutMillis = 40000 // 15 seconds
        }
    }
    suspend fun run(input: String, username: String): String{
        val credentials = ChatJson(input, username)


        val response = client.post("http://13.60.104.145:8080/agentProcess") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }

        // Log della risposta del server
        val responseBody = response.bodyAsText()
        println("Risposta dell'Agente: $responseBody")
        return responseBody
    }
}