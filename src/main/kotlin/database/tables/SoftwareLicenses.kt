package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object SoftwareLicenses : Table("software_licenses") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val vendorId = integer("vendor_id").references(Vendors.id).nullable()
    val licenseKey = varchar("license_key", 255).nullable()
    val startDate = date("start_date").nullable()
    val expiryDate = date("expiry_date").nullable()
    val numberOfSeats = integer("number_of_seats").nullable()
    val seatsUsed = integer("seats_used").default(0)
    val assignedTo = integer("assigned_to").references(Users.id).nullable()
    val notes = text("notes").nullable()
    val status = varchar("status", 50).default("ACTIVE")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}