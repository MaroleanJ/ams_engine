package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Subscriptions : Table("subscriptions") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val vendorId = integer("vendor_id").references(Vendors.id).nullable()
    val plan = varchar("plan", 100).nullable()
    val startDate = date("start_date").nullable()
    val expiryDate = date("expiry_date").nullable()
    val renewalDate = date("renewal_date").nullable()
    val cost = decimal("cost", 10, 2).nullable()
    val billingCycle = varchar("billing_cycle", 20).nullable()
    val assignedTo = integer("assigned_to").references(Users.id).nullable()
    val notes = text("notes").nullable()
    val status = varchar("status", 50).default("ACTIVE")
    val autoRenewal = bool("auto_renewal").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}