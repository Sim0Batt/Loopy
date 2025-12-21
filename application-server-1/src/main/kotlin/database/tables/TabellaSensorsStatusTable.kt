package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaSensorsStatusTable: IntIdTable("SensorsStatus") {
    val ppgStatus = varchar("PPGStatus", 2)
    val accelerometerStatus = varchar("AccelerometerStatus", 2)
    val electrodeStatus = varchar("ElectrodeStatus", 2)
    val thermometerStatus = varchar("ThermometerStatus", 2)
    val userId = integer("userId")
    val timestamp = varchar("timestamp", 100)

}