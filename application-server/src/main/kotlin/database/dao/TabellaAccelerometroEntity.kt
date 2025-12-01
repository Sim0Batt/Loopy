package database.dao

import database.tables.TabellaAccelerometroTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaAccelerometroEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaAccelerometroEntity>(TabellaAccelerometroTable)

    var acc_x by TabellaAccelerometroTable.acc_x
    var acc_y by TabellaAccelerometroTable.acc_y
    var acc_z by TabellaAccelerometroTable.acc_z
    var userId by TabellaAccelerometroTable.userId
    var timestamp by TabellaAccelerometroTable.timestamp
}
