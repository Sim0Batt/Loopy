package database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConfig(): Database{
        return Database.connect(
            url = "jdbc:mysql://localhost:3306/LoopyDB?allowPublicKeyRetrieval=true&useSSL=false",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "pws",
        )
    }
}