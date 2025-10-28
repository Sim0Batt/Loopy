package com.example.loopy.devicemanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DeviceManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dm_activity)

        /*------------------TASTI NAVBAR--------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_dm

        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dm -> {
                    true
                }

                R.id.nav_data -> {
                    val intent = Intent(this@DeviceManagerActivity, DataActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_chatbot -> {
                    val intent = Intent(this@DeviceManagerActivity, ChatActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_home -> {
                    val intent = Intent(this@DeviceManagerActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this@DeviceManagerActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        /* ----------------------------------------------------------------------------------------- */
    }
    override fun onResume() {
        super.onResume()
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNavBar.selectedItemId = R.id.nav_dm
    }
}