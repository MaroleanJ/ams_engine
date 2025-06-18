package com.techbros.plugins

import com.techbros.config.DatabaseConfig
import com.techbros.database.tables.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val database = DatabaseConfig.init()

    // Create tables
    transaction(database) {
        SchemaUtils.create(
            Users
        )
    }
}