package database.dao

import database.tables.TabellaElettrodiTable
import database.tables.TabellaSleepTable
import database.tables.TabellaStressTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaStressEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaStressEntity>(TabellaSleepTable)
    var stressLevel by TabellaStressTable.stressLevel
    var userId by TabellaStressTable.userId
    var timestamp by TabellaStressTable.timestamp
}