package database.dao

import database.tables.TabellaAccelerometroTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaAccelerometroEntity (id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<TabellaAccelerometroEntity>(TabellaAccelerometroTable)
    var movimento by TabellaAccelerometroTable.movimento
    var userId by TabellaAccelerometroTable.userId
    var timestamp by TabellaAccelerometroTable.timestamp
}