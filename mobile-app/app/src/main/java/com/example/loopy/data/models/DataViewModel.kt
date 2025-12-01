package com.example.loopy.data.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopy.data.models.DataDisplay
import com.example.loopy.data.models.input.SensorDataJson
import com.example.loopy.data.models.input.SummaryDataJson
import com.example.loopy.network.KtorClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    private val client = KtorClient.client
    // qui ho creato ste robe cosi se per caso cambiamo http per la decima volta non è un problema :)
    private val baseUrl = "http://51.21.196.187:8080"
    private val endpointRecente = "$baseUrl/getDatas"
    private val endpointRiepilogo = "$baseUrl/getSummary"

    private val _displayData = MutableLiveData<DataDisplay>()
    val displayData: LiveData<DataDisplay> = _displayData
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // vabbe funzione che gestisce le chiamate per prendere i dati
    fun caricaDatiUtente(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                Log.d("DataViewModel", "Avvio chiamate parallele per userId: $userId")

                val datiRecentiJob = async {
                    Log.d("DataViewModel", "Chiamo: $endpointRecente/$userId")
                    client.get("$endpointRecente/$userId").body<SensorDataJson>()
                }
                val datiRiepilogoJob = async {
                    Log.d("DataViewModel", "Chiamo: $endpointRiepilogo/$userId")
                    client.get("$endpointRiepilogo/$userId").body<SummaryDataJson>()
                }

                // le ho fatte con await cosi non si sfancula tutto se magari l'utente gira il telefono
                val datiRecenti = datiRecentiJob.await()
                val datiRiepilogo = datiRiepilogoJob.await()

                Log.d("DataViewModel", "Dati recenti e riepilogo scaricati.")

                // Combina i risultati dei due lavori
                val datiPuliti = combinaDati(datiRecenti, datiRiepilogo)

                // Scrivo in bacheca
                _displayData.postValue(datiPuliti)

            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Errore di rete: ${e.message}")
            }
        }
    }

    private fun combinaDati(datiRecenti: SensorDataJson, datiRiepilogo: SummaryDataJson): DataDisplay {

        //Dati semplici
        val hrAttuale = datiRecenti.heartRates.split(',').lastOrNull()?.trim() ?: "N/D"
        val spo2Attuale = datiRecenti.oxygens.split(',').lastOrNull()?.trim() ?: "N/D"
        val tempAttuale = datiRecenti.temperatures.split(',').lastOrNull()?.trim() ?: "N/D"
        val sweatAttuale = datiRecenti.sweatings.split(',').lastOrNull()?.trim() ?: "N/D"

        // funzione che converte minuti in h
        fun formattaMinuti(minuti: Int?): String {
            if (minuti == null || minuti == 0) return "N/D"
            return "${minuti / 60}h ${minuti % 60}m"
        }

        
        val hrvCalcolato = datiRiepilogo.hrv?.toString() ?: "N/D"
        val rhrCalcolato = datiRiepilogo.rhr_a_riposo?.toString() ?: "N/D"
        val recuperoCalcolato = datiRiepilogo.recupero?.toString() ?: "N/D"

        val sonnoTotale = formattaMinuti(datiRiepilogo.sonno_totale_minuti)
        val sonnoProfondo = formattaMinuti(datiRiepilogo.sonno_profondo_minuti)
        val sonnoRem = formattaMinuti(datiRiepilogo.sonno_rem_minuti)

        val attivitaIntensa = formattaMinuti(datiRiepilogo.attivita_intensa_minuti)
        val attivitaModerata = formattaMinuti(datiRiepilogo.attivita_moderata_minuti)
        val attivitaLeggera = formattaMinuti(datiRiepilogo.attivita_leggera_minuti)
        val attivitaSedentaria = formattaMinuti(datiRiepilogo.attivita_sedentaria_minuti)

        val stressAlto = formattaMinuti(datiRiepilogo.stress_alto_minuti)
        val stressMedio = formattaMinuti(datiRiepilogo.stress_medio_minuti)
        val stressCalmo = formattaMinuti(datiRiepilogo.stress_calmo_minuti)

        // oggetto finale da mostrare
        return DataDisplay(
            // semplivci
            hrValue = "$hrAttuale bpm",
            spo2Value = "$spo2Attuale %",
            tempValue = "$tempAttuale °C",
            sweatValue = sweatAttuale,

            //calcolati

            // Recupero
            hrvValue = "$hrvCalcolato ms",
            rhrValue = "$rhrCalcolato bpm",
            recuperoValue = "$recuperoCalcolato / 100",

            // Sonno
            sonnoTotale = sonnoTotale,
            sonnoProfondo = sonnoProfondo,
            sonnoRem = sonnoRem,
            sonnoGraficoJson = datiRiepilogo.sonno_grafico_json,

            // Attività
            attivitaIntensa = attivitaIntensa,
            attivitaModerata = attivitaModerata,
            attivitaLeggera = attivitaLeggera,
            attivitaSedentaria = attivitaSedentaria,
            attivitaGraficoJson = datiRiepilogo.attivita_grafico_json,

            // Stress
            stressAlto = stressAlto,
            stressMedio = stressMedio,
            stressCalmo = stressCalmo,
            stressGraficoJson = datiRiepilogo.stress_grafico_json

            // (glucoseValue, ecc.
        )
    }
}