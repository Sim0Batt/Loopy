package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaPpgTable: IntIdTable("PPG") {
    val battito = integer("battito")
    val ossigenazione = varchar("ossigenazione", 10)
    val timestamp = varchar("timestamp", 100)
}