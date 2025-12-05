package com.example.loopy.devicemanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.data.DataActivity
import com.example.loopy.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.loopy.core.BaseActivity
import com.example.loopy.devicemanager.models.StatusJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json as KotlinxJson

class DeviceManagerActivity : BaseActivity() {
    private val client = HttpClient (CIO) {
        install(ContentNegotiation) {
            json(KotlinxJson {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dm_activity)

        val deviceStatusText = findViewById<TextView>(R.id.deviceStatus)
        val ppgStatusText = findViewById<TextView>(R.id.ppgStatus)
        val accelerometerStatusText = findViewById<TextView>(R.id.accelerometerStatus)
        val thermometerStatusText = findViewById<TextView>(R.id.thermometerStatus)
        val electrodesStatusText = findViewById<TextView>(R.id.electrodesStatus)
        val reloadButton = findViewById<Button>(R.id.button)

        reloadButton.setOnClickListener {
            lifecycleScope.launch{
                try {
                    val response = client.get("http://51.20.84.226:8080/status")

                    val responseBody = response.bodyAsText()
                    Log.d("Device Manager", "Server Response: $responseBody")
                    println("Server response: $responseBody") //responseBody contiene success o failure

                    runOnUiThread {
                        try {
                            if(getDeviceStatus(responseBody, ppgStatusText, accelerometerStatusText, thermometerStatusText, electrodesStatusText) == "SUCCESS"){
                                Toast.makeText(this@DeviceManagerActivity, "All services are running", Toast.LENGTH_LONG).show()
                                Log.d("Device Manager", "All services are running")
                            }else{
                                deviceStatusText.setTextColor(resources.getColor(R.color.red))
                                deviceStatusText.text = "FAILURE"
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@DeviceManagerActivity, "Device Refresh Failed: Invalid response", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Device Manager", "Errore durante la richiesta di login", e)
                    runOnUiThread {
                        Toast.makeText(this@DeviceManagerActivity, "Errore di rete: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch{
            try {
                val response = client.get("http://51.20.84.226:8080/status/1")

                val responseBody = response.bodyAsText()
                Log.d("Device Manager", "Server Response: $responseBody")
                println("Server response: $responseBody") //responseBody contiene success o failure

                runOnUiThread {
                    try {
                        if(getDeviceStatus(responseBody, ppgStatusText, accelerometerStatusText, thermometerStatusText, electrodesStatusText) == "SUCCESS"){
                            deviceStatusText.text = "SUCCESS"
                            deviceStatusText.setTextColor(resources.getColor(R.color.green))

                            Toast.makeText(this@DeviceManagerActivity, "All services are running", Toast.LENGTH_LONG).show()
                            Log.d("Device Manager", "All services are running")
                        }else{
                            deviceStatusText.setTextColor(resources.getColor(R.color.red))
                            deviceStatusText.text = "FAILURE"
                            getSensorsStatus(ppgStatusText, accelerometerStatusText, thermometerStatusText, electrodesStatusText)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DeviceManagerActivity, "Device Refresh Failed: Invalid response", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Login", "Errore durante la richiesta di status", e)
                runOnUiThread {
                    Toast.makeText(this@DeviceManagerActivity, "Errore di rete: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }










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

    @SuppressLint("SetTextI18n")
    fun getDeviceStatus(
        responseBody: String,
        ppgStatusText: TextView,
        accelerometerStatusText: TextView,
        thermometerStatusText: TextView,
        electrodesStatusText: TextView
    ): String{
        val statusJson = KotlinxJson.decodeFromString<StatusJson>(responseBody)
        var result: Boolean = true

        if(statusJson.ppgStatus == "OK") {
            ppgStatusText.text = "SUCCESS"
            ppgStatusText.setTextColor(resources.getColor(R.color.green))
        }else{
            ppgStatusText.text = "FAILURE"
            ppgStatusText.setTextColor(resources.getColor(R.color.red))
            result = false
        }
        if(statusJson.accelerometerStatus == "OK") {
            accelerometerStatusText.text = "SUCCESS"
            accelerometerStatusText.setTextColor(resources.getColor(R.color.green))
        }else{
            accelerometerStatusText.text = "FAILURE"
            accelerometerStatusText.setTextColor(resources.getColor(R.color.red))
            result = false

        }
        if(statusJson.thermometerStatus == "OK") {
            thermometerStatusText.text = "SUCCESS"
            thermometerStatusText.setTextColor(resources.getColor(R.color.green))
        }else{
            thermometerStatusText.text = "FAILURE"
            thermometerStatusText.setTextColor(resources.getColor(R.color.red))
            result = false
        }
        if(statusJson.electrodesStatus == "OK") {
            electrodesStatusText.text = "SUCCESS"
            electrodesStatusText.setTextColor(resources.getColor(R.color.green))
        }else{
            electrodesStatusText.text = "FAILURE"
            electrodesStatusText.setTextColor(resources.getColor(R.color.red))
            result = false
        }

        Log.d("Device Manager", "Success Sensors: $result")
        return if(result){
            "SUCCESS"
        }else{
            "FAILURE"
        }
    }

    @SuppressLint("SetTextI18n")
    fun getSensorsStatus(ppgStatusText: TextView, accelerometerStatusText: TextView, thermometerStatusText: TextView, electrodesStatusText: TextView){
        runBlocking {
            val ppgSensor = client.get("http://172.20.10.6:8080/ppg")
            val ppgStatus = ppgSensor.bodyAsText()
            if(ppgStatus == "FAILURE"){
                ppgStatusText.text = "FAILURE"
                ppgStatusText.setTextColor(resources.getColor(R.color.red))
            }else{
                ppgStatusText.text = "SUCCESS"
                ppgStatusText.setTextColor(resources.getColor(R.color.green))
            }


            val electrodesSensor = client.get("http://172.20.10.6:8080/electrodes")
            val electrodesStatus = electrodesSensor.bodyAsText()
            if(electrodesStatus == "FAILURE"){
                electrodesStatusText.text = "FAILURE"
                electrodesStatusText.setTextColor(resources.getColor(R.color.red))
            }else{
                electrodesStatusText.text = "SUCCESS"
                electrodesStatusText.setTextColor(resources.getColor(R.color.green))
            }


            val thermometerSensor = client.get("http://172.20.10.6:8080/termometer")
            val thermometerStatus = thermometerSensor.bodyAsText()
            if(thermometerStatus == "FAILURE"){
                thermometerStatusText.text = "FAILURE"
                thermometerStatusText.setTextColor(resources.getColor(R.color.red))
            }else{
                thermometerStatusText.text = "SUCCESS"
                thermometerStatusText.setTextColor(resources.getColor(R.color.green))
            }


            val accelerometerSensor = client.get("http://172.20.10.6:8080/accelerometer")
            val accelerometerStatus = accelerometerSensor.bodyAsText()
            if(accelerometerStatus == "FAILURE"){
                accelerometerStatusText.text = "FAILURE"
                accelerometerStatusText.setTextColor(resources.getColor(R.color.red))
            }else{
                accelerometerStatusText.text = "SUCCESS"
                accelerometerStatusText.setTextColor(resources.getColor(R.color.green))
            }
        }
    }
}