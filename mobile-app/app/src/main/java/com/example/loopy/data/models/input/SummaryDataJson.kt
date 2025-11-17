package com.example.loopy.data.models.input

import kotlinx.serialization.Serializable
import android.annotation.SuppressLint

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SummaryDataJson(
    val hrv: Int? = null,
    val stress: Int? = null,
    val passi: Int? = null,
    val recupero: Int? = null,
    val vo2max: Int? = null,
    // TODO: da ricontrollare con cose reali
    //TODO: da implementare per bene
    val hr_a_riposo: Int? = null,
    val rhr_a_riposo: Int? = null,
    val sonno_totale_minuti: Int? = null,
    val sonno_profondo_minuti: Int? = null,
    val sonno_rem_minuti: Int? = null,
    val attivita_intensa_minuti: Int? = null,
    val attivita_moderata_minuti: Int? = null,
    val attivita_leggera_minuti: Int? = null,
    val attivita_sedentaria_minuti: Int? = null,
    val stress_alto_minuti: Int? = null,
    val stress_medio_minuti: Int? = null,
    val stress_calmo_minuti: Int? = null,
    val sonno_grafico_json: String = "",
    val attivita_grafico_json: String = "",
    val stress_grafico_json: String = "",

)