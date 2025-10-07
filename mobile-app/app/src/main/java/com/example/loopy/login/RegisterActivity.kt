package com.example.loopy.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.login.models.RegisterJson
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
import kotlin.toString

class RegisterActivity: ComponentActivity() {
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
        setContentView(R.layout.register_activity)

        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val submitButton = findViewById<ImageButton>(R.id.submitButton)
        val registerButton = findViewById<ImageButton>(R.id.registerButton)
        val inputUsername = findViewById<EditText>(R.id.usernameInput)
        val inputAge = findViewById<EditText>(R.id.ageInput)
        val inputWeight = findViewById<EditText>(R.id.weightInput)
        val inputHeight = findViewById<EditText>(R.id.heightInput)
        val inputSex = findViewById<EditText>(R.id.sexInput)


        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val username = inputUsername.text.toString()
            val age = inputAge.text.toString().toInt()
            val weight = inputWeight.text.toString().toInt()
            val height = inputHeight.text.toString().toInt()
            val sex = inputSex.text.toString()
            Log.d("Login", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")

            val credentials = RegisterJson(
                username,
                email,
                password,
                age,
                height,
                weight,
                sex
            )

            lifecycleScope.launch{
                try {
                    val response = client.post ("http://16.171.169.80:8080/register") {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(credentials)
                    }

                    // Log della risposta del server
                    val responseBody = response.bodyAsText()
                    Log.d("Login", "Risposta del server: $responseBody")
                    println("Risposta del server: $responseBody") //responseBody contiene success o failure

                    runOnUiThread {
                        if(responseBody == "success"){
                            Toast.makeText(this@RegisterActivity, "Register Success",
                                Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        }

                    }

                } catch (e: Exception) {
                    // Gestione degli errori di rete
                    Log.e("Login", "Errore durante la richiesta di login", e)
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Errore di rete: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


}

