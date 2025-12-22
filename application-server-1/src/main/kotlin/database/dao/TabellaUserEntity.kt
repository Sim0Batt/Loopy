package database.dao


import database.tables.TabellaUserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaUserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaUserEntity>(TabellaUserTable)
    var email by TabellaUserTable.email
    var password by TabellaUserTable.password
    var username by TabellaUserTable.username
    var height by TabellaUserTable.height
    var weight by TabellaUserTable.weight
    var age by TabellaUserTable.age
    var sex by TabellaUserTable.sex
}