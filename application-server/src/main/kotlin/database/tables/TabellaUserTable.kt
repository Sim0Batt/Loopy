package database.tables

import database.dao.TabellaUserEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaUserTable : IntIdTable("User") {
    val email = varchar("email", 100)
    val password = varchar("password", 1)
}