package com.example.loopy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.IntentCompat
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.settings.SettingsActivity
import com.example.loopy.ui.theme.LoopyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val profilePictures = findViewById<ImageButton>(R.id.profilePicture)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        val helloUser =  findViewById<TextView>(R.id.helloUser)
        val chatbotCloud = findViewById<ImageButton>(R.id.chatbotCloud) /* nuvoletta */
        val chatbotButton = findViewById<ImageButton>(R.id.chatbotButton) /* bottone */
        val dataButton = findViewById<ImageButton>(R.id.dataButton)
        val deviceManager = findViewById<ImageButton>(R.id.dmbutton)
        val graphView = findViewById<ImageButton>(R.id.dataGraph) /* da controllare per switching */
        val kcalView = findViewById<Button>(R.id.dailyConsumption) /* rivedere il nome */
        val hbView = findViewById<Button>(R.id.heartBeat)

        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        profilePictures.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        /*chatbotButton.setOnClickListener {
            val intent = Intent(this@MainActivity, chatBotActivity::class.java)
            startActivity(intent)
        }*/

       /* chatbotCloud.setOnClickListener {
            val intent = Intent(this@MainActivity, chatBotActivity::class.java)
            startActivity(intent)
        }*/

        dataButton.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            startActivity(intent)
        }

        deviceManager.setOnClickListener {
            val intent = Intent(this@MainActivity, DeviceManagerActivity::class.java)
            startActivity(intent)
        }

        kcalView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            startActivity(intent)
        }

        hbView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            startActivity(intent)
        }














    }
}