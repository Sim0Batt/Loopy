package com.example.loopy.data.models.input // O il tuo package corretto

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SummaryDataJson(
    // Dati base
    val hrv: Int? = null,
    val rhr_a_riposo: Int? = null,
    val recupero: Int? = null,
    val vo2max: Int? = null,

    // Sonno
    val sonno_totale_minuti: Int? = null,
    val sonno_profondo_minuti: Int? = null,
    val sonno_leggero_minuti: Int? = null,
    val sonno_rem_minuti: Int? = null,

    // Attività Diurna
    val attivita_sedentaria_minuti: Int? = null,
    val attivita_leggera_minuti: Int? = null,
    val attivita_moderata_minuti: Int? = null,
    val attivita_intensa_minuti: Int? = null,

    // Stress Diurno
    val stress_calmo_minuti: Int? = null,
    val stress_medio_minuti: Int? = null,
    val stress_alto_minuti: Int? = null,
)