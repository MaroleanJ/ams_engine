package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table

object MaintenanceTypes : Table("maintenance_types") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val estimatedDurationHours = integer("estimated_duration_hours").nullable()
    val costEstimate = decimal("cost_estimate", 10, 2).nullable()

    override val primaryKey = PrimaryKey(id)
}