package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table

object AssetCategories : Table("asset_categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}