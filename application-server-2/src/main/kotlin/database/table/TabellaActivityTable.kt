package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaActivityTable: IntIdTable("Activity") {
    val activityLevel = integer("activity_level")
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 30)
}