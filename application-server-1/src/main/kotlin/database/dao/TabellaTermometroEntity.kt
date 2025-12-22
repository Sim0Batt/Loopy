package database.dao

import database.tables.TabellaTermometroTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaTermometroEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaTermometroEntity>(TabellaTermometroTable)
    var temperatura by TabellaTermometroTable.temperatura
    var userId by TabellaTermometroTable.userId
    var timestamp by TabellaTermometroTable.timestamp
}