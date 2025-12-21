package database.dao

import database.tables.TabellaGlucosioTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaGlucosioEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaGlucosioEntity>(TabellaGlucosioTable)
    var glicemia by TabellaGlucosioTable.glicemia
    var userId by TabellaGlucosioTable.userId
    var timestamp by TabellaGlucosioTable.timestamp
}