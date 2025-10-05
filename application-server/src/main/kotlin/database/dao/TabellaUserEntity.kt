package database.dao


import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaUserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaUserEntity>(TabellaUserTable)
    var email by TabellaUserTable.email
    var password by TabellaUserTable.password
}