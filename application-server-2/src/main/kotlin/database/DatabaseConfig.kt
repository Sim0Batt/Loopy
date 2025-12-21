package org.example.database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConfig(): Database {
        return Database.Companion.connect(
            url = "jdbc:mariadb://localhost:3306/LoopyDB?allowPublicKeyRetrieval=true&useSSL=false",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "loopy",
        )
    }
}