package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaAccelerometroTable : IntIdTable("Accelerometro") {
    val acc_x = double("acc_x")
    val acc_y = double("acc_y")
    val acc_z = double("acc_z")

    val userId = integer("userId")
    val timestamp = varchar("timestamp", 100)
}
