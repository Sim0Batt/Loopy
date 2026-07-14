package com.example.loopy

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.example.loopy.data.models.input.ReturnDataJson
import com.example.loopy.data.models.input.ReturnSSAGDataJson
import com.example.loopy.profile.json.UserDataJson
import com.example.loopy.utils.APPLICATION_SERVER_1_IP
import com.example.loopy.utils.APPLICATION_SERVER_2_IP
import com.example.loopy.utils.GraphsAdapter
import com.example.loopy.utils.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
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
        val recoveryView = findViewById<TextView>(R.id.dailyConsumption)
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

        recoveryView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            startActivity(intent)
        }

        hbView.setOnClickListener {
            val intent = Intent(this@MainActivity, DataActivity::class.java)
            startActivity(intent)
        }

        /*------------------ASYNCHRONOUS LOGIC--------------------*/
        lifecycleScope.launch {
            //Circle Valuse Retrieve
            val userData = client.post("http://${APPLICATION_SERVER_1_IP}/user/$userId").body<UserDataJson>()
            calculateCircleValues(client, recoveryView, hbView, userId, userData)

            //Image Retrive
            viewPager.adapter = GraphsAdapter(downloadImagesBitmap(client, userId))

            //Agent Summary
            try{
                val agentResponse = ChatCaller.run(BEGINNING_PROMPT, userId)
                updateChatBotMessage(chatbotCloud, agentResponse)
            }catch (e: Exception){
                Log.d("HomePage", "Error during agent summary")
            }
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
            var serverResponse = client.get("http://${APPLICATION_SERVER_2_IP}:8080/generateGraph/stress/$userId")
            var imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://${APPLICATION_SERVER_2_IP}:8080/generateGraph/sleep/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://${APPLICATION_SERVER_2_IP}:8080/generateGraph/activity/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        }catch (e: Exception){
            Log.d("HomePage", "Error during graphs download")
        }
        return tmp
    }

    private suspend fun calculateCircleValues(client: HttpClient, recoveryText: TextView, heartRateText: TextView, userId : Int, userData: UserDataJson){
        try{
            val app1Data = client.get("http://${APPLICATION_SERVER_1_IP}:8080/getDatas/$userId").body<ReturnDataJson>()
            val app2Data = client.get("http://${APPLICATION_SERVER_2_IP}:8080/getSSAGData/$userId").body<ReturnSSAGDataJson>()
            heartRateText.text = app1Data.heartRate

            var basal = if(userData.gender == "M"){
                66.5 + (13.75 * userData.weight.toDouble()) + (5.0 * userData.height.toDouble()) - (6.775 * userData.age.toInt())
            }else{
                655.1 + (9.563 * userData.weight.toDouble()) + (1.85 * userData.height.toDouble()) - (4.676 * userData.age.toInt())
            }

            recoveryText.text = when(app2Data.activity){
                "Sedentary" -> (basal * 1.2).toInt().toString()
                "Light" -> (basal * 1.375).toInt().toString()
                "Moderate" -> (basal * 1.55).toInt().toString()
                "Intense" -> (basal * 1.725).toInt().toString()
                else -> (basal * 1.2).toInt().toString()
            }
        }catch (e: Exception){
            Log.d("HomePage", "Error during circle values calculation")
        }
    }

}