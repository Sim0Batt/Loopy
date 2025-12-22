package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaSleepTable: IntIdTable("Sleep") {
    val sleepLevel = integer("sleep_level")
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 30)
}