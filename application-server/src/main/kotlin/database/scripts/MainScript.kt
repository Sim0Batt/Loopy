package database.scripts

import database.DatabaseConfig
import database.tables.TabellaUserTable
import database.tables.TabellaUserTable.age
import database.tables.TabellaUserTable.email
import database.tables.TabellaUserTable.height
import database.tables.TabellaUserTable.password
import database.tables.TabellaUserTable.sex
import database.tables.TabellaUserTable.username
import database.tables.TabellaUserTable.weight
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.models.RegisterJson

class MainScript {
    fun getUsers(): String{
        var users = ""
        transaction(DatabaseConfig().getConfig()) {
            TabellaUserTable.selectAll().map {
                users += it[TabellaUserTable.id].toString() + ", " + it[TabellaUserTable.email] + ", " +
                        it[TabellaUserTable.password] + ", " +
                        it[TabellaUserTable.username] + ", " +
                        it[TabellaUserTable.age] + ", " +
                        it[TabellaUserTable.height] + ", " +
                        it[TabellaUserTable.weight] + "\n"

            }
        }
        return users
    }

    fun registerUser(credentials: RegisterJson){
        transaction(DatabaseConfig().getConfig()) {
            TabellaUserTable.insert {
                it[email] = credentials.email
                it[password] = credentials.password
                it[username] = credentials.username
                it[age] = credentials.age
                it[height] = credentials.height
                it[weight] = credentials.weight
                it[sex] = credentials.sex
            }
        }
    }
}