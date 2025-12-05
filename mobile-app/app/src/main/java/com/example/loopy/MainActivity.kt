package com.example.loopy

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.loopy.chat.ChatActivity
import com.example.loopy.chat.scripts.ChatCaller
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.loopy.core.BaseActivity
import com.example.loopy.utils.GraphsAdapter
import com.example.loopy.utils.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json as KotlinxJson


class MainActivity : BaseActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val client = HttpClient (CIO) {
            install(ContentNegotiation) {
                json(KotlinxJson {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }


        val userId = SessionManager.currentUserId!!
        val username = SessionManager.currentUsername

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val profilePictures = findViewById<ImageButton>(R.id.profilePicture)
        val helloUser = findViewById<TextView>(R.id.helloUser)
        val chatbotCloud = findViewById<TextView>(R.id.chatbotCloud)
        val kcalView = findViewById<TextView>(R.id.dailyConsumption)
        val hbView = findViewById<TextView>(R.id.heartBeat)
        val viewPager = findViewById<ViewPager2>(R.id.graphsViewPager)

        helloUser.text = "Welcome $username"

        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        profilePictures.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        chatbotCloud.setOnClickListener {
            val intent = Intent(this@MainActivity, ChatActivity::class.java)
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

        /*------------------ASYNCHRONOUS LOGIC--------------------*/
        lifecycleScope.launch {
            val agentResponse = ChatCaller().run(BEGINNING_PROMPT, "Simone")
            updateChatBotMessage(chatbotCloud, agentResponse)

            //Image Retrive
            viewPager.adapter = GraphsAdapter(downloadImagesBitmap(client, userId))
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
                    startActivity(intent)
                    true
                }

                R.id.nav_chatbot -> {
                    val intent = Intent(this@MainActivity, ChatActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_dm -> {
                    val intent = Intent(this@MainActivity, DeviceManagerActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
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

    private fun updateChatBotMessage(textView: TextView, message: String){
        textView.text = message
    }

    private val BEGINNING_PROMPT = """
        Give me a full checkup in base of my data IN MAXIMUM 15 WORDS.
    """.trimIndent()

    private suspend fun downloadImagesBitmap(client: HttpClient, userId: Int): List<Bitmap> {
        val tmp: MutableList<Bitmap> = mutableListOf()
        try {
            var serverResponse = client.get("http://13.60.38.181:8080/generateGraph/stress/$userId")
            var imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://13.60.38.181:8080/generateGraph/sleep/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://13.60.38.181:8080/generateGraph/activity/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        }catch (e: Exception){
            Log.d("HomePage", "Error during graphs download")
        }
        return tmp
    }

}