package com.example.loopy.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.loopy.R
import com.example.loopy.login.models.UserJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class LoginActivity : ComponentActivity() {
    private val client = HttpClient (CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val submitButton = findViewById<ImageButton>(R.id.submitButton)
        val registerButton = findViewById<ImageButton>(R.id.registerButton)


        registerButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Registration", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")

        }

        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Login", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")

            val credentials = UserJson(email, password)

            lifecycleScope.launch{
                try {
                    val response = client.post ("http://16.171.169.80:8080/login") {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(credentials)
                    }

                    // Log della risposta del server
                    val responseBody = response.bodyAsText()
                    Log.d("Login", "Risposta del server: $responseBody")
                    println("Risposta del server: $responseBody") //responseBody contiene success o failure

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login attempt finished. See logs for details.",
                            Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    // Gestione degli errori di rete
                    Log.e("Login", "Errore durante la richiesta di login", e)
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Errore di rete: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
