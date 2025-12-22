package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaElettrodiTable: IntIdTable("Elettrodi") {
    val sudorazione = double("sudorazione")
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 100)
}