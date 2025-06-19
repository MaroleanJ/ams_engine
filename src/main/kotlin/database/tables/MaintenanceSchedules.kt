package com.techbros.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object MaintenanceSchedules : Table("maintenance_schedules") {
    val id = integer("id").autoIncrement()
    val assetId = integer("asset_id").references(Assets.id, onDelete = ReferenceOption.CASCADE)
    val maintenanceTypeId = integer("maintenance_type_id").references(MaintenanceTypes.id).nullable()
    val maintenanceType = varchar("maintenance_type", 100).nullable()
    val frequencyDays = integer("frequency_days")
    val lastPerformed = date("last_performed").nullable()
    val nextDue = date("next_due")
    val assignedTo = integer("assigned_to").references(Users.id).nullable()
    val priority = varchar("priority", 20).nullable()
    val estimatedCost = decimal("estimated_cost", 10, 2).nullable()
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}