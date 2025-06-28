package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object AssetIssues : Table("asset_issues") {
    val id = integer("id").autoIncrement()
    val assetId = integer("asset_id").references(Assets.id)
    val reportedBy = integer("reported_by").references(Users.id)
    val assignedTo = integer("assigned_to").references(Users.id).nullable()
    val issueType = varchar("issue_type", 50)
    val severity = varchar("severity", 20)
    val issueDescription = text("issue_description")
    val resolutionNotes = text("resolution_notes").nullable()
    val status = varchar("status", 50).default("OPEN")
    val reportedAt = datetime("reported_at").default(LocalDateTime.now())
    val resolvedAt = datetime("resolved_at").nullable()
    val closedAt = datetime("closed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}