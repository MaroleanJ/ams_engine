package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table

object Vendors : Table("vendors") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val contactEmail = varchar("contact_email", 255).nullable()
    val contactPhone = varchar("contact_phone", 20).nullable()
    val address = text("address").nullable()

    override val primaryKey = PrimaryKey(id)
}