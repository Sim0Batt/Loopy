package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaGlucosioTable: IntIdTable("Glucosio") {
    val glicemia = double("glicemia")
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 100)
}