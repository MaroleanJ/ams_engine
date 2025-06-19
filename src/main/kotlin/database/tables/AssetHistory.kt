package com.techbros.database.tables

import com.techbros.database.tables.Assets.defaultExpression
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object AssetHistory : Table("asset_history") {
    val id = integer("id").autoIncrement()
    val assetId = integer("asset_id").references(Assets.id, onDelete = ReferenceOption.CASCADE)
    val changedBy = integer("changed_by").references(Users.id)
    val actionType = varchar("action_type", 50)
    val fieldChanged = varchar("field_changed", 100).nullable()
    val oldValue = text("old_value").nullable()
    val newValue = text("new_value").nullable()
    val description = text("description").nullable()
    val changedAt = timestamp("changed_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}