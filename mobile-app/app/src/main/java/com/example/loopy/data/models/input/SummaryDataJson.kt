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
    val vo2max: Int? = null
    // TODO: da ricontrollare con cose reali
)