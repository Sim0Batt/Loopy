package com.example.loopy.main

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
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.chat.scripts.ChatCaller
import com.example.loopy.core.BaseActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.data.models.input.DailyDataJson
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.login.models.input.UserJson
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.profile.json.UserDataJson
import com.example.loopy.settings.SettingsActivity
import com.example.loopy.utils.GraphsAdapter
import com.example.loopy.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.readBytes
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : BaseActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
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
            // Data Circles Update
            val dailyDataJson = client.get("http://13.60.104.145:8080/getDatas/$userId").body<DailyDataJson>()
//            val userDataJson = client.post("http://13.60.104.145:8080/user/$userId").body<UserDataJson>()
//            updateCircleData(hbView, kcalView, dailyDataJson, userDataJson)

            //Image Retrive
            viewPager.adapter = GraphsAdapter(downloadImagesBitmap(client, userId))

            //Agent Response
            val agentResponse = ChatCaller().run(BEGINNING_PROMPT, "Simone")
            updateChatBotMessage(chatbotCloud, agentResponse)
        }



        //tasti nav-bar
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNavBar.selectedItemId = R.id.nav_home

        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_data -> { startActivity(Intent(this, DataActivity::class.java)); finish(); true }
                R.id.nav_chatbot -> { startActivity(Intent(this, ChatActivity::class.java)); finish(); true }
                R.id.nav_dm -> { startActivity(Intent(this, DeviceManagerActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }

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

    private fun updateCircleData(hrTextView: TextView, dcTextView: TextView, dataJson: DailyDataJson, userDataJson: UserDataJson){
        hrTextView.text = dataJson.heartRate
        dcTextView.text = calculateDailyConsumption(userDataJson, dataJson).toString()
    }

    private fun calculateDailyConsumption(userJson: UserDataJson, dailyDataJson: DailyDataJson): Double{
        val basal = if(userJson.gender == "M"){
            66.5 + (13.75 * userJson.weight.toInt()) + (5.003 * userJson.height.toInt()) - (6.755 * userJson.age.toInt())
        }else{
            655.1 + (9.5663 * userJson.weight.toInt()) + (1.85 * userJson.height.toInt()) - (4.676 * userJson.age.toInt())
        }
        return when(dailyDataJson.activity){
            "Sedentary" -> {
                basal * 1.2
            }
            "Light" -> {
                basal * 1.375
            }
            "Moderate" -> {
                basal * 1.55
            }
            "High" -> {
                basal * 1.85
            }
            else -> basal * 1.2
        }
    }

    private val BEGINNING_PROMPT = """
        Give me a full checkup in base of my data IN MAXIMUM 15 WORDS.
    """.trimIndent()

    private suspend fun downloadImagesBitmap(client: HttpClient, userId: Int): List<Bitmap> {
        val tmp: MutableList<Bitmap> = mutableListOf()
        try {
            var serverResponse = client.get("http://13.60.104.145:8080/generateGraph/stress/$userId")
            var imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://13.60.104.145:8080/generateGraph/sleep/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))

            serverResponse = client.get("http://13.60.104.145:8080/generateGraph/activity/$userId")
            imageBytes = serverResponse.readBytes()
            tmp.add(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        }catch (e: Exception){
            Log.d("HomePage", "Error during graphs download")
        }
        return tmp
    }

}