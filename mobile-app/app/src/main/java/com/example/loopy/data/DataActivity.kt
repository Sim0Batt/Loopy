package com.example.loopy.data

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_activity)

        val hrValue = findViewById<TextView>(R.id.hrValue)
        val hrvValue = findViewById<TextView>(R.id.hrvValue)
        val spo2Value = findViewById<TextView>(R.id.spo2Value)
        val activityValue = findViewById<TextView>(R.id.activityValue)
        val tempValue = findViewById<TextView>(R.id.tempValue)
        val sweatValue = findViewById<TextView>(R.id.sweatValue)
        val vo2Value = findViewById<TextView>(R.id.vo2Value)
        val sleepValue = findViewById<TextView>(R.id.sleepValue)
        val stressValue = findViewById<TextView>(R.id.stressValue)
        val recoveryValue = findViewById<TextView>(R.id.recoveryValue)
        val glucoseValue = findViewById<TextView>(R.id.glucoseValue)


        /* ------------------------TASTI NAVBAR----------------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_data

        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_data -> {
                    true
                }

                R.id.nav_home -> {
                    val intent = Intent(this@DataActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_chatbot -> {
                    val intent = Intent(this@DataActivity, ChatActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_dm -> {
                    val intent = Intent(this@DataActivity, DeviceManagerActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this@DataActivity, ProfileActivity::class.java)
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
        bottomNavBar.selectedItemId = R.id.nav_data
    }
}