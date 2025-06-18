package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Assets : Table("assets") {
    val id = integer("id").autoIncrement()
    val categoryId = integer("category_id").references(AssetCategories.id)
    val vendorId = integer("vendor_id").references(Vendors.id).nullable()
    val locationId = integer("location_id").references(Locations.id).nullable()
    val name = varchar("name", 255)
    val model = varchar("model", 100).nullable()
    val serialNumber = varchar("serial_number", 100).nullable().uniqueIndex()
    val barcode = varchar("barcode", 100).nullable().uniqueIndex()
    val purchaseDate = date("purchase_date").nullable()
    val purchasePrice = decimal("purchase_price", 10, 2).nullable()
    val currentValue = decimal("current_value", 10, 2).nullable()
    val depreciationRate = decimal("depreciation_rate", 5, 2).nullable()
    val warrantyExpiry = date("warranty_expiry").nullable()
    val assignedTo = integer("assigned_to").references(Users.id).nullable()
    val status = varchar("status", 50).nullable()
    val location = text("location").nullable()
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}