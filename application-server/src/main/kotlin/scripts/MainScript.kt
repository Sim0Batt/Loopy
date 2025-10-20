package scripts

import database.DatabaseConfig
import database.tables.TabellaUserTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.RegisterJson

class MainScript {
    fun getUsers(): String{
        var users = ""
        transaction(DatabaseConfig.getConfig()) {
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
        transaction(DatabaseConfig.getConfig()) {
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