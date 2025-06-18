package com.techbros.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun init(): Database {
        return Database.connect(
            url = "jdbc:postgresql://dpg-d18mm3ali9vc73fnpbn0-a.oregon-postgres.render.com:5432/ams_fhnq",
            driver = "org.postgresql.Driver",
            user = "ams",
            password = "RMJwQsjgM4VYhlqfzcG8oqYXUVyZTw34"
        )
    }
}