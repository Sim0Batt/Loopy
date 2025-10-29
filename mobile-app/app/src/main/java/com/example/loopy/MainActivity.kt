package com.example.loopy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.loopy.chat.ChatActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val bundle = intent.extras
        val USER_ID = bundle?.getInt("USER_ID")
        Log.d("MainActivity", "USER_ID: ${USER_ID.toString()}")


        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val profilePictures = findViewById<ImageButton>(R.id.profilePicture)
        val helloUser = findViewById<TextView>(R.id.helloUser)
        val chatbotCloud = findViewById<ImageButton>(R.id.chatbotCloud) /* nuvoletta */
        val graphView = findViewById<ImageButton>(R.id.dataGraph) /* da controllare per switching */
        val kcalView = findViewById<Button>(R.id.dailyConsumption) /* rivedere il nome */
        val hbView = findViewById<Button>(R.id.heartBeat)

        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            intent.putExtra("USER_ID", USER_ID)
            startActivity(intent)
        }

        profilePictures.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            intent.putExtra("USER_ID", USER_ID)
            startActivity(intent)
        }

        chatbotCloud.setOnClickListener { // Questo è la nuvoletta, giusto? SI
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
            intent.putExtra("USER_ID", USER_ID)
            startActivity(intent)
        }

        kcalView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            intent.putExtra("USER_ID", USER_ID)
            startActivity(intent)
        }

        hbView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            intent.putExtra("USER_ID", USER_ID)
            startActivity(intent)
        }


        /*------------------TASTI NAVBAR--------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_home

        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }

                R.id.nav_data -> {
                    val intent = Intent(this@MainActivity, DataActivity::class.java)
                    intent.putExtra("USER_ID", USER_ID)
                    startActivity(intent)
                    true
                }

                R.id.nav_chatbot -> {
                    val intent = Intent(this@MainActivity, ChatActivity::class.java)
                    intent.putExtra("USER_ID", USER_ID)
                    startActivity(intent)
                    true
                }

                R.id.nav_dm -> {
                    val intent = Intent(this@MainActivity, DeviceManagerActivity::class.java)
                    intent.putExtra("USER_ID", USER_ID)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra("USER_ID", USER_ID)
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
        bottomNavBar.selectedItemId = R.id.nav_home
    }
}