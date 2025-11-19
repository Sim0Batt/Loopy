package database.dao

import database.tables.TabellaAccelerometroTable
import database.tables.TabellaSensorsStatusTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaSensorsStatusEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<TabellaSensorsStatusEntity>(TabellaSensorsStatusTable)
    var ppgStatus by TabellaSensorsStatusTable.ppgStatus
    var accelerometerStatus by TabellaSensorsStatusTable.accelerometerStatus
    var electrodeStatus by TabellaSensorsStatusTable.electrodeStatus
    var thermometerStatus by TabellaSensorsStatusTable.thermometerStatus
    var userId by TabellaSensorsStatusTable.userId
    var timestamp by TabellaSensorsStatusTable.timestamp
}