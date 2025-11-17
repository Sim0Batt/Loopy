package com.example.loopy.data.models
data class DataDisplay(
    // dati semplici
    val hrValue: String = "N/D",
    val spo2Value: String = "N/D",
    val tempValue: String = "N/D",
    val sweatValue: String = "N/D",

    // dati calcolati
    //Recupero
    val hrvValue: String = "N/D",
    val rhrValue: String = "N/D",
    val recuperoValue: String = "N/D",

    //Sonno
    val sonnoTotale: String = "N/D",
    val sonnoProfondo: String = "N/D",
    val sonnoRem: String = "N/D",
    val sonnoGraficoJson: String? = null,

    //Attivita
    val attivitaIntensa: String = "N/D",
    val attivitaModerata: String = "N/D",
    val attivitaLeggera: String = "N/D",
    val attivitaSedentaria: String = "N/D",
    val attivitaGraficoJson: String? = null,

    //stress
    val stressAlto: String = "N/D",
    val stressMedio: String = "N/D",
    val stressCalmo: String = "N/D",
    val stressGraficoJson: String? = null,

    // val glucoseValue: String = "N/D"

    //TODO: da implementare
    val activityValue: String = "N/D",
    val vo2Value: String = "N/D",
    val sleepValue: String = "N/D",
    val stressValue: String? = null,
    val recoveryValue: String = "N/D",
    val glucoseValue: String? = null,
)