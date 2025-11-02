package com.example.loopy.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.login.models.input.UserJson
import com.example.loopy.login.models.output.AccountJson
import com.example.loopy.utils.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json as KotlinxJson

class LoginActivity : ComponentActivity() {
    private val client = HttpClient (CIO) {
        install(ContentNegotiation) {
            json(KotlinxJson {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val registerButton = findViewById<Button>(R.id.registerButton)


        registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)

        }

        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Login", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")

            val credentials = UserJson(email, password)

            lifecycleScope.launch{
                try {
                    val response = client.post ("http://13.60.241.135:8080/login") {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(credentials)
                    }

                    // Log della risposta del server
                    val responseBody = response.bodyAsText()
                    Log.d("Login", "Risposta del server: $responseBody")
                    println("Risposta del server: $responseBody") //responseBody contiene success o failure

                    runOnUiThread {
                        try {
                            // Parse the JSON response {"userId": id} and save in object S.M.
                            val loginResponse = KotlinxJson.decodeFromString<AccountJson>(responseBody)
                            SessionManager.currentUserId = loginResponse.userId

                            Toast.makeText(this@LoginActivity, "Login Success",
                                Toast.LENGTH_LONG).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@LoginActivity, "Login Failed: Invalid response", Toast.LENGTH_LONG).show()
                        }
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
