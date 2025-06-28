package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object MaintenanceRecords : Table("maintenance_records") {
    val id = integer("id").autoIncrement()
    val assetId = integer("asset_id").references(Assets.id)
    val scheduleId = integer("schedule_id").references(MaintenanceSchedules.id).nullable()
    val performedBy = integer("performed_by").references(Users.id)
    val maintenanceType = varchar("maintenance_type", 100)
    val performedDate = date("performed_date")
    val durationHours = decimal("duration_hours", 5, 2).nullable()
    val cost = decimal("cost", 10, 2).nullable()
    val description = text("description").nullable()
    val partsReplaced = text("parts_replaced").nullable()
    val nextMaintenanceDue = date("next_maintenance_due").nullable()
    val status = varchar("status", 50).default("COMPLETED")
    val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}