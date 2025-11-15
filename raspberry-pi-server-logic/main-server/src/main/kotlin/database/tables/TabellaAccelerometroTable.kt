package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaAccelerometroTable: IntIdTable("Accelerometro") {
    val movimento = bool("movimento")
    val timestamp = varchar("timestamp", 100)
}