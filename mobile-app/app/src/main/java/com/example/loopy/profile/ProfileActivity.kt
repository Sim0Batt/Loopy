package com.example.loopy.profile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.loopy.core.BaseActivity
import com.example.loopy.profile.json.UserDataJson
import com.example.loopy.utils.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json as KotlinxJson

class ProfileActivity : BaseActivity() {

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
        setContentView(R.layout.profile_activity)

        val userId = SessionManager.currentUserId!!

        /* ------------------------TASTI NAVBAR----------------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_profile

        val usernameText = findViewById<TextView>(R.id.usernameText)
        val emailText = findViewById<TextView>(R.id.emailText)
        val ageText = findViewById<TextView>(R.id.ageText)
        val sexText = findViewById<TextView>(R.id.sexText)
        val weightText = findViewById<TextView>(R.id.weightText)
        val heightText = findViewById<TextView>(R.id.heightText)


        lifecycleScope.launch {
            val response = client.post("http://51.20.84.226:8080/user/$userId")
            val responseBody = response.bodyAsText()
            println(responseBody)
            val userJson = KotlinxJson.decodeFromString<UserDataJson>(responseBody)

            usernameText.text = userJson.username
            emailText.text = userJson.email
            ageText.text = userJson.age
            sexText.text = userJson.gender
            weightText.text = userJson.weight
            heightText.text = userJson.height
        }





        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    true
                }

                R.id.nav_home -> {
                    val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_chatbot -> {
                    val intent = Intent(this@ProfileActivity, ChatActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_dm -> {
                    val intent = Intent(this@ProfileActivity, DeviceManagerActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_data -> {
                    val intent = Intent(this@ProfileActivity, DataActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNavBar.selectedItemId = R.id.nav_profile
    }
}