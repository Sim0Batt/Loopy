package database.dao

import database.tables.TabellaActivityTable
import database.tables.TabellaElettrodiTable
import database.tables.TabellaSleepTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaActivityEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaActivityEntity>(TabellaActivityTable)
    var activityLevel by TabellaActivityTable.activityLevel
    var userId by TabellaActivityTable.userId
    var timestamp by TabellaActivityTable.timestamp
}