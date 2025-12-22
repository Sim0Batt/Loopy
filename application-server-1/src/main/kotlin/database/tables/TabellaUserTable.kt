package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaUserTable : IntIdTable("User") {
    val username = varchar("username", 100)
    val email = varchar("email", 100)
    val password = varchar("password", 100)
    val height = integer("height")
    val weight = integer("weight")
    val age = integer("age")
    val sex = varchar("sex", 100)
}