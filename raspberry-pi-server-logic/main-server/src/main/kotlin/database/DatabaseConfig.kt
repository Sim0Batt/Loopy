package database

import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun getConnection(): Database{
        return Database.connect(
            url = "jdbc:mysql://localhost:3306/LoopyDB",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "Simone04"
        )
    }

}