package server.jsonModels.outputJsons

import kotlinx.serialization.Serializable
// il json con i dati compessi, non è finito ma prima devo implementare gli algoritmi .py
@Serializable
data class SummaryDataJson(
    val hrv: Int?,
    val stress: Int?,
    val passi: Int?,
    val recupero: Int?,
    val vo2max: Int?
    // TODO: da ricontrollare con cose reali
)