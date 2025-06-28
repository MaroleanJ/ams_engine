package com.techbros.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object FileAttachments : Table("file_attachments") {
    val id = integer("id").autoIncrement()
    val relatedTable = varchar("related_table", 100)
    val relatedId = integer("related_id")
    val fileName = varchar("file_name", 255)
    val fileUrl = text("file_url")
    val fileSize = long("file_size").nullable()
    val fileType = varchar("file_type", 100).nullable()
    val uploadedBy = integer("uploaded_by").references(Users.id)
    val uploadedAt = datetime("uploaded_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}