package com.example.loopy.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.login.models.input.RegisterJson
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json as KotlinxJson

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
        setContentView(R.layout.register_activity)
        val ageList: List<Int> = RegisterListSet().setAgeList()
        val heightList: List<Int> = RegisterListSet().setHeightList()
        val weightList: List<Int> = RegisterListSet().setWeightList()
        val sexList: List<String> = listOf("M", "F")


        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val inputUsername = findViewById<EditText>(R.id.usernameInput)

        val inputAge = findViewById<Spinner>(R.id.ageInput)
        var adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ageList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputAge.adapter = adapter


        val inputWeight = findViewById<Spinner>(R.id.weightInput)
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            weightList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputWeight.adapter = adapter

        val inputHeight = findViewById<Spinner>(R.id.heightInput)
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            heightList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputHeight.adapter = adapter

        val inputSex = findViewById<Spinner>(R.id.sexInput)
        val sexAdapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            sexList
        )
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputSex.adapter = sexAdapter



        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val username = inputUsername.text.toString()
            val age = inputAge.selectedItem.toString().toInt()
            val weight = inputWeight.selectedItem.toString().toInt()
            val height = inputHeight.selectedItem.toString().toInt()
            val sex = inputSex.selectedItem.toString()
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

            println(credentials.toString())

            lifecycleScope.launch{
                try {
                    val response = client.post ("http://51.20.84.226:8080/register") {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(credentials)
                    }

                    // Log della risposta del server
                    val responseBody = response.bodyAsText()
                    Log.d("Register", "Risposta del server: $responseBody")
                    println("Risposta del server: $responseBody") //responseBody contiene success o failure

                    runOnUiThread {
                        try {
                            // Parse the JSON response {"userId": id}
                            val loginResponse = KotlinxJson.decodeFromString<AccountJson>(responseBody)
                            Toast.makeText(this@RegisterActivity, "Registration Success",
                                Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            // Optionally pass the userId to the next activity
                            SessionManager.currentUserId = loginResponse.userId
                            SessionManager.currentUsername = loginResponse.username
                            startActivity(intent)
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this@RegisterActivity, "Login Failed: Invalid response", Toast.LENGTH_LONG).show()
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

