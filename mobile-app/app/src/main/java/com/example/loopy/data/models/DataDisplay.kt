package com.example.loopy.data.models

//qui ho messo sta roba per avere le robe pulite da mandare alla dataaActivity
//TODO: eventualmente sono da sistemare na voltaa che abbiamo i dati reali che ci arrivano
data class DataDisplay(
    val hrValue: String = "N/D",
    val hrvValue: String = "N/D",
    val spo2Value: String = "N/D",
    val activityValue: String = "N/D",
    val tempValue: String = "N/D",
    val sweatValue: String = "N/D",
    val vo2Value: String = "N/D",
    val sleepValue: String = "N/D",
    val stressValue: String = "N/D",
    val recoveryValue: String = "N/D",
    val glucoseValue: String = "N/D"
)