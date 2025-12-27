package com.example.loopy.settings

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
import com.example.loopy.profile.json.UserDataJson
import com.example.loopy.utils.APPLICATION_SERVER_1_IP
import com.example.loopy.utils.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class EditAccountActivity : ComponentActivity() {

    private val client = HttpClient(CIO) {
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
        setContentView(R.layout.edit_account_activity)

        val listSet = EditAccountListSet()
        val ageList: List<Int> = listSet.setAgeList()
        val heightList: List<Int> = listSet.setHeightList()
        val weightList: List<Int> = listSet.setWeightList()
        val sexList: List<String> = listOf("M", "F")

        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val inputUsername = findViewById<EditText>(R.id.usernameInput)
        val saveButton = findViewById<Button>(R.id.ConfirmButton)

        val inputAge = findViewById<Spinner>(R.id.ageInput)
        val inputWeight = findViewById<Spinner>(R.id.weightInput)
        val inputHeight = findViewById<Spinner>(R.id.heightInput)
        val inputSex = findViewById<Spinner>(R.id.sexInput)

        inputAge.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ageList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        inputWeight.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weightList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        inputHeight.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, heightList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        inputSex.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sexList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val userId = SessionManager.currentUserId

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val url = "http://$APPLICATION_SERVER_1_IP:8080/user/$userId"
                    val response = client.post(url)
                    val responseBody = response.bodyAsText()

                    val userData = Json.decodeFromString<UserDataJson>(responseBody)

                    runOnUiThread {
                        inputUsername.setText(userData.username)
                        inputEmail.setText(userData.email)

                        try {
                            val ageIndex = ageList.indexOf(userData.age.toInt())
                            if (ageIndex >= 0) inputAge.setSelection(ageIndex)

                            val weightIndex = weightList.indexOf(userData.weight.toInt())
                            if (weightIndex >= 0) inputWeight.setSelection(weightIndex)

                            val heightIndex = heightList.indexOf(userData.height.toInt())
                            if (heightIndex >= 0) inputHeight.setSelection(heightIndex)

                            val sexIndex = sexList.indexOf(userData.gender)
                            if (sexIndex >= 0) inputSex.setSelection(sexIndex)
                        } catch (e: Exception) {
                            Log.e("EditAccount", "Error converting numbers or finding indices", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EditAccount", "Error downloading user data", e)
                }
            }
        } else {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val username = inputUsername.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in Username, Email, and Password!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (inputAge.selectedItem == null || inputWeight.selectedItem == null ||
                inputHeight.selectedItem == null || inputSex.selectedItem == null) {
                Toast.makeText(this, "Data missing, please wait for lists to load", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val age = inputAge.selectedItem.toString().toInt()
            val weight = inputWeight.selectedItem.toString().toInt()
            val height = inputHeight.selectedItem.toString().toInt()
            val sex = inputSex.selectedItem.toString()

            val updateData = RegisterJson(
                email = email,
                password = password,
                username = username,
                age = age,
                weight = weight,
                height = height,
                sex = sex
            )

            lifecycleScope.launch {
                try {
                    val updateUrl = "http://$APPLICATION_SERVER_1_IP:8080/editUser/$userId"
                    Log.d("EditAccount", "Updating to: $updateUrl")

                    val response = client.post(updateUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(updateData)
                    }

                    val responseBody = response.bodyAsText()

                    if (response.status == HttpStatusCode.OK || responseBody.contains("Updated", ignoreCase = true)) {
                        runOnUiThread {
                            Toast.makeText(this@EditAccountActivity, "Profile updated!", Toast.LENGTH_LONG).show()

                            SessionManager.currentUsername = username

                            val intent = Intent(this@EditAccountActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@EditAccountActivity, "Error: $responseBody", Toast.LENGTH_LONG).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("EditAccount", "Error saving data", e)
                    runOnUiThread {
                        Toast.makeText(this@EditAccountActivity, "Connection error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}