package com.example.loopy.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


object KtorClient {

    val client = HttpClient(CIO) {

        install(HttpTimeout) {
            connectTimeoutMillis = 15000 // 15 secondi per connettersi
            requestTimeoutMillis = 15000 // 15 secondi per ricevere una risposta
        }


        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                allowSpecialFloatingPointValues = true
            })
        }
    }
}