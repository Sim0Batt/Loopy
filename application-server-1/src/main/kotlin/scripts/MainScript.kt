package scripts

import database.DatabaseConfig
import database.tables.TabellaElettrodiTable
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.RegisterJson
import java.io.File
import java.util.concurrent.TimeUnit

object MainScript {
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

    fun registerUser(credentials: RegisterJson): Int{
        var userId = 0
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

            userId = TabellaUserTable.selectAll().where {
                TabellaUserTable.email eq credentials.email
            }.firstOrNull()?.getOrNull(TabellaUserTable.id).toString().toIntOrNull() ?: throw Exception("User not registered")
        }
        return userId
    }
}