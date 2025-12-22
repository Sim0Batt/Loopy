package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaStressTable: IntIdTable("Stress") {
    val stressLevel = integer("stress_level")
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 30)
}