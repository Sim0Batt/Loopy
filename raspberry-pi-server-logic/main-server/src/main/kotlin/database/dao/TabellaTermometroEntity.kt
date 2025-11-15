package database.dao

import database.tables.TabellaTermometroTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaTermometroEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaTermometroEntity>(TabellaTermometroTable)
    var temperatura by TabellaTermometroTable.temperatura
    var timestamp by TabellaTermometroTable.timestamp
}