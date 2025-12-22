package com.example.loopy.data.models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loopy.data.models.input.ReturnDataJson
import com.example.loopy.network.KtorClient
import com.example.loopy.utils.APPLICATION_SERVER_1_IP
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class DataViewModel : ViewModel() {

    private val client = KtorClient.client

    private val _displayData = MutableLiveData<DataDisplay>()
    val displayData: LiveData<DataDisplay> = _displayData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun caricaDatiUtente(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DataViewModel", "Calling: http://${APPLICATION_SERVER_1_IP}:8080/getDatas/$userId")

                val daily = client.get("http://${APPLICATION_SERVER_1_IP}:8080/getDatas/$userId").body<ReturnDataJson>()

                val display = mapDailyToDisplay(daily)

                _displayData.postValue(display)

            } catch (e: Exception) {
                e.printStackTrace()
                _error.postValue("Network/parsing error: ${e.message}")
            }
        }
    }

    private fun mapDailyToDisplay(d: ReturnDataJson): DataDisplay {
        val hr = d.heartRate.toDoubleSafe()
        val spo2 = d.oxygen.toDoubleSafe()
        val temp = d.temperature.toDoubleSafe()
        val sweat = d.sweating.toDoubleSafe()
        val gluc = d.glucose.toDoubleSafe()

        return DataDisplay(
            hrValue = "${fmt0(hr)} bpm",
            spo2Value = "${fmt1(spo2)} %",
            tempValue = "${fmt1(temp)} °C",
            sweatValue = fmt1(sweat),

            activityValue = d.activity,
            stressValue = d.stress,
            sleepValue = d.sleep,
            glucoseValue = fmt0(gluc),

            hrvValue = "—",
            vo2Value = "—",
            recoveryValue = "—"
        )
    }

    private fun String.toDoubleSafe(): Double? =
        this.replace(',', '.').toDoubleOrNull()

    private fun fmt0(x: Double?): String =
        x?.let { "%.0f".format(it) } ?: "—"

    private fun fmt1(x: Double?): String =
        x?.let { String.format(Locale.US, "%.1f", it) } ?: "—"

}
