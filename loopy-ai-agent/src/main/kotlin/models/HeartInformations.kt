package models

class HeartInformations (
    val heartRate: Int,
    val oxigenation: Int,
    val timestamp: String,
){
    override fun toString(): String {
        return "heartRate=$heartRate, oxigenation=$oxigenation, timestamp=$timestamp"
    }
}