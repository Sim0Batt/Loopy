package com.example.loopy.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        /* ------------------------TASTI NAVBAR----------------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_profile

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