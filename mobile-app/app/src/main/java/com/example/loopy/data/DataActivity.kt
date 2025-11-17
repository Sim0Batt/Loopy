package com.example.loopy.data

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.loopy.data.models.DataViewModel
import androidx.activity.viewModels
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.ChatActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.loopy.core.BaseActivity
import com.example.loopy.data.models.DataDisplay
import com.example.loopy.utils.SessionManager
import java.lang.Error


class DataActivity : BaseActivity() {

    private val viewModel: DataViewModel by viewModels()

    private lateinit var hrValue: TextView
    private lateinit var hrvValue: TextView
    private lateinit var spo2Value: TextView
    private lateinit var activityValue: TextView
    private lateinit var tempValue: TextView
    private lateinit var sweatValue: TextView
    private lateinit var vo2Value: TextView
    private lateinit var sleepValue: TextView
    private lateinit var stressValue: TextView
    private lateinit var recoveryValue: TextView
    private lateinit var glucoseValue: TextView
    private lateinit var bottomNavBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_activity)

        initViews()
        setupBottomNavBar()
        setupObservers()


        val userId = SessionManager.currentUserId
        if (userId != null) {
            viewModel.caricaDatiUtente(userId.toString())
        } else {
            Toast.makeText(this, "Errore: Utente non trovato", Toast.LENGTH_LONG).show()
            // finish()
        }
    }

    private fun initViews() {
        hrValue = findViewById(R.id.hrValue)
        hrvValue = findViewById(R.id.hrvValue)
        spo2Value = findViewById(R.id.spo2Value)
        activityValue = findViewById(R.id.activityValue)
        tempValue = findViewById(R.id.tempValue)
        sweatValue = findViewById(R.id.sweatValue)
        vo2Value = findViewById(R.id.vo2Value)
        sleepValue = findViewById(R.id.sleepValue)
        stressValue = findViewById(R.id.stressValue)
        recoveryValue = findViewById(R.id.recoveryValue)
        glucoseValue = findViewById(R.id.glucoseValue)
        bottomNavBar = findViewById(R.id.bottomNavBar)
    }


    private fun setupObservers() {
        viewModel.displayData.observe(this) { dati ->
            if (dati != null) {
                Log.d("DataActivity", "Dati ricevuti dal ViewModel, aggiorno UI")
                updateUI(dati)
            }
        }

        viewModel.error.observe(this) { errore ->
            if (errore != null) {
                Log.e("DataActivity", "Errore dal ViewModel: $errore")
                Toast.makeText(this, errore, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(data: DataDisplay) {
        hrValue.text = data.hrValue
        hrvValue.text = data.hrvValue
        spo2Value.text = data.spo2Value
        activityValue.text = data.activityValue
        tempValue.text = data.tempValue
        sweatValue.text = data.sweatValue
        vo2Value.text = data.vo2Value
        sleepValue.text = data.sleepValue
        stressValue.text = data.stressValue
        recoveryValue.text = data.recoveryValue
        glucoseValue.text = data.glucoseValue
    }

    private fun setupBottomNavBar() {
        bottomNavBar.selectedItemId = R.id.nav_data
        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_data -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_chatbot -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_dm -> {
                    startActivity(Intent(this, DeviceManagerActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    // sta roba serve a non far baggare il coso che mostra in che activity siamo nella navbar
    override fun onResume() {
        super.onResume()
        if (::bottomNavBar.isInitialized) {
            bottomNavBar.selectedItemId = R.id.nav_data
        }
    }
}

// TODO: gestire meglio quello che arriva con glucosio... cambiare logica, (forse?) per ora l'ho messo come gli altri valori