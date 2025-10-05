package com.example.loopy.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.loopy.R
import com.example.loopy.configurations.ReadXMLResources
import configuration.DatabaseConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class LoginActivity : ComponentActivity() {

    // 1. Declare config but initialize it in onCreate
    private lateinit var config: DatabaseConfiguration

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)

        // 2. Initialize config here, where context is available
        // Pass 'this' (the Activity context) to your resource reader
        config = ReadXMLResources().run()

        val inputEmail = findViewById<EditText>(R.id.emailInput)
        val inputPassword = findViewById<EditText>(R.id.passwordInput)
        val submitButton = findViewById<ImageButton>(R.id.submitButton)
        val registerButton = findViewById<ImageButton>(R.id.registerButton)


        registerButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Registration", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")
            // You can call your database logic here
        }

        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Login", "Email: $email, Password: $password")
            println("Email: $email, Password: $password")
            // 3. Launch a coroutine for database operations
            lifecycleScope.launch {
                try {
                    val isValid = connectAndValidateUser(email, password)
                    if (isValid) {
                        Log.d("Login", "Login Successful")
                        // TODO: Navigate to the next screen
                    } else {
                        Log.d("Login", "Invalid credentials")
                        // TODO: Show an error message to the user
                    }
                } catch (e: Exception) {
                    Log.e("Login", "Database operation failed", e)
                    // TODO: Handle connection errors
                }
            }
        }
    }

    // 4. Create a suspend function to handle database logic off the main thread
    private suspend fun connectAndValidateUser(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) { // Switch to a background thread
            val connection = Database.connect(
                url = config.databaseUrl,
                driver = "com.mysql.cj.jdbc.Driver",
                user = config.databaseUser,
                password = config.databasePassword
            )

            // Example of a transaction. Replace with your actual user validation logic.
            transaction(connection) {
                // val user = UserTable.select { UserTable.email eq email }.singleOrNull()
                // return@transaction user?.password == password
                true // Placeholder
            }
        }
    }
}
