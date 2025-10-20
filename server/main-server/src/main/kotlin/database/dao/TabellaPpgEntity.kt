package database.dao

import database.tables.TabellaPpgTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaPpgEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaPpgEntity>(TabellaPpgTable)
    var battito by TabellaPpgTable.battito
    var ossigenazione by TabellaPpgTable.ossigenazione
    var timestamp by TabellaPpgTable.timestamp
}