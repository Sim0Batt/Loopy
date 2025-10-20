package database.dao

import database.tables.TabellaElettrodiTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaElettrodiEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaElettrodiEntity>(TabellaElettrodiTable)
    var sudorazione by TabellaElettrodiTable.sudorazione
    var timestamp by TabellaElettrodiTable.timestamp
}