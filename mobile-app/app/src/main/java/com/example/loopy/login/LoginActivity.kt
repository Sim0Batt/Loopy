package com.example.loopy.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.loopy.R

class LoginActivity : ComponentActivity() {
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
        }
        submitButton.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            Log.d("Login", "Email: $email, Password: $password")
        }

    }

}
