package database.dao

import database.tables.TabellaSleepTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaSleepEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaSleepEntity>(TabellaSleepTable)
    var sleepLevel by TabellaSleepTable.sleepLevel
    var userId by TabellaSleepTable.userId
    var timestamp by TabellaSleepTable.timestamp
}