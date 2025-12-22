package database.dao

import database.tables.TabellaPpgTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaPpgEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaPpgEntity>(TabellaPpgTable)
    var battito by TabellaPpgTable.battito
    var ossigenazione by TabellaPpgTable.ossigenazione
    var userId by TabellaPpgTable.userId
    var timestamp by TabellaPpgTable.timestamp
}