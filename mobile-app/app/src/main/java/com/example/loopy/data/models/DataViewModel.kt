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

    //vabbe funzione che gestisce le chiamate per prendere i dati
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
                // le ho fatte con await cosi non si sfancula tutto se magari l'utente gira il telefono o
                // fa qualsiasi altra cosa... non blocco gli altri thread
                val datiRecenti = datiRecentiJob.await()
                val datiRiepilogo = datiRiepilogoJob.await()

                Log.d("DataViewModel", "Dati recenti e riepilogo scaricati.")

                val datiPuliti = combinaDati(datiRecenti, datiRiepilogo)

                _displayData.postValue(datiPuliti)

            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Errore di rete: ${e.message}")
            }
        }
    }


    private fun combinaDati(datiRecenti: SensorDataJson, datiRiepilogo: SummaryDataJson): DataDisplay {

        val hrAttuale = datiRecenti.heartRates.split(',').lastOrNull()?.trim() ?: "N/D"
        val spo2Attuale = datiRecenti.oxygens.split(',').lastOrNull()?.trim() ?: "N/D"
        val tempAttuale = datiRecenti.temperatures.split(',').lastOrNull()?.trim() ?: "N/D"
        val sweatAttuale = datiRecenti.sweatings.split(',').lastOrNull()?.trim() ?: "N/D"

        val hrvCalcolato = datiRiepilogo.hrv?.toString() ?: "N/D"
        val stressCalcolato = datiRiepilogo.stress?.toString() ?: "N/D"
        val passiCalcolati = datiRiepilogo.passi?.toString() ?: "N/D"
        val recuperoCalcolato = datiRiepilogo.recupero?.toString() ?: "N/D"
        val vo2maxCalcolato = datiRiepilogo.vo2max?.toString() ?: "N/D"

        return DataDisplay(
            hrValue = "$hrAttuale bpm",
            spo2Value = "$spo2Attuale %",
            tempValue = "$tempAttuale °C",
            sweatValue = sweatAttuale,

            hrvValue = "$hrvCalcolato ms",
            stressValue = "$stressCalcolato / 100",
            activityValue = "$passiCalcolati passi",
            recoveryValue = "$recuperoCalcolato / 100",
            vo2Value = "$vo2maxCalcolato ml/kg/min",
            // TODO: questi da sistemare, apsetto a capire cosa effettivamente ci arriva
            // TODO: sistemare anche eventuali cose come ml/kg/min ecc... dipende da cosa otterrò con gli alg.
            sleepValue = "78 / 100",
            glucoseValue = "Normale"
        )
    }
}



// SPIEGAZIONE GENERALE PER SIMON:
// in caricaDatiUtente faccio le chiamate al server per prendere dati istantanei e quelli complessi " riepilogo"
// notare che lo faccio in modo asincrono cosi non perdo tempo nelle chiamate che verrebbero "distrutte" se ad esempio l'utente gira il telefono
// combino i dati istantanei e quelli complessi, notare che in caso i dati complessi siano nulli ( perche magari non abbiamo abbastanza valori)
// uscira N/D ( non disponibile).
// Scrivo in "bacheca" questi dati con _displayData... cosi appena abbiamo qualcosa bussa alla DataActivity e quella osserva i dati con displayData