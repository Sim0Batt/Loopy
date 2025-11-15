package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaTermometroTable: IntIdTable("Termometro") {
    val temperatura = double("temperatura")
    val timestamp = varchar("timestamp", 100)
}