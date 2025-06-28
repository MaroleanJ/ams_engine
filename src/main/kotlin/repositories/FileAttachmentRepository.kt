package com.techbros.repositories

import com.techbros.database.tables.FileAttachments
import com.techbros.database.tables.Users
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class FileAttachmentRepository {

    suspend fun create(request: CreateFileAttachmentRequest): Int = dbQuery {
        FileAttachments.insert {
            it[relatedTable] = request.relatedTable
            it[relatedId] = request.relatedId
            it[fileName] = request.fileName
            it[fileUrl] = request.fileUrl
            it[fileSize] = request.fileSize
            it[fileType] = request.fileType
            it[uploadedBy] = request.uploadedBy
        }[FileAttachments.id]
    }

    suspend fun bulkCreate(requests: List<CreateFileAttachmentRequest>): List<Int> = dbQuery {
        requests.map { request ->
            FileAttachments.insert {
                it[relatedTable] = request.relatedTable
                it[relatedId] = request.relatedId
                it[fileName] = request.fileName
                it[fileUrl] = request.fileUrl
                it[fileSize] = request.fileSize
                it[fileType] = request.fileType
                it[uploadedBy] = request.uploadedBy
            }[FileAttachments.id]
        }
    }

    suspend fun findById(id: Int): FileAttachmentDto? = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .where { FileAttachments.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<FileAttachmentDto> = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .orderBy(FileAttachments.uploadedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateFileAttachmentRequest): Boolean = dbQuery {
        FileAttachments.update({ FileAttachments.id eq id }) {
            it[relatedTable] = request.relatedTable
            it[relatedId] = request.relatedId
            it[fileName] = request.fileName
            it[fileUrl] = request.fileUrl
            it[fileSize] = request.fileSize
            it[fileType] = request.fileType
            it[uploadedBy] = request.uploadedBy
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        FileAttachments.deleteWhere { FileAttachments.id eq id } > 0
    }

    suspend fun findByEntity(relatedTable: String, relatedId: Int): List<FileAttachmentDto> = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .where {
                (FileAttachments.relatedTable eq relatedTable) and
                        (FileAttachments.relatedId eq relatedId)
            }
            .orderBy(FileAttachments.uploadedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByTable(relatedTable: String): List<FileAttachmentDto> = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .where { FileAttachments.relatedTable eq relatedTable }
            .orderBy(FileAttachments.uploadedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByUploadedBy(uploadedBy: Int): List<FileAttachmentDto> = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .where { FileAttachments.uploadedBy eq uploadedBy }
            .orderBy(FileAttachments.uploadedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByFileType(fileType: String): List<FileAttachmentDto> = dbQuery {
        FileAttachments
            .join(Users, JoinType.LEFT, FileAttachments.uploadedBy, Users.id)
            .selectAll()
            .where { FileAttachments.fileType eq fileType }
            .orderBy(FileAttachments.uploadedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun deleteByEntity(relatedTable: String, relatedId: Int): Int = dbQuery {
        FileAttachments.deleteWhere {
            (FileAttachments.relatedTable eq relatedTable) and
                    (FileAttachments.relatedId eq relatedId)
        }
    }

    suspend fun deleteByTable(relatedTable: String): Int = dbQuery {
        FileAttachments.deleteWhere { FileAttachments.relatedTable eq relatedTable }
    }

    suspend fun deleteByUploadedBy(uploadedBy: Int): Int = dbQuery {
        FileAttachments.deleteWhere { FileAttachments.uploadedBy eq uploadedBy }
    }

    suspend fun getFileAttachmentsByEntity(relatedTable: String, relatedId: Int): FileAttachmentsByEntityDto = dbQuery {
        val attachments = findByEntity(relatedTable, relatedId)
        val totalSize = attachments.mapNotNull { it.fileSize }.sum()

        FileAttachmentsByEntityDto(
            relatedTable = relatedTable,
            relatedId = relatedId,
            totalFiles = attachments.size,
            totalSize = if (totalSize > 0) totalSize else null,
            attachments = attachments
        )
    }

    suspend fun getFileAttachmentStats(): FileAttachmentStatsDto = dbQuery {
        val allFiles = FileAttachments.selectAll().toList()

        val totalSize = allFiles.mapNotNull { it[FileAttachments.fileSize] }.sum()

        val filesByType = allFiles.groupBy { it[FileAttachments.fileType] ?: "unknown" }
            .mapValues { it.value.size }

        val filesByTable = allFiles.groupBy { it[FileAttachments.relatedTable] }
            .mapValues { it.value.size }

        val filesByUploader = allFiles.groupBy { it[FileAttachments.uploadedBy] }
            .mapValues { it.value.size }

        val averageFileSize = if (allFiles.isNotEmpty() && totalSize > 0) {
            totalSize / allFiles.size
        } else null

        FileAttachmentStatsDto(
            totalFiles = allFiles.size,
            totalSize = if (totalSize > 0) totalSize else null,
            filesByType = filesByType,
            filesByTable = filesByTable,
            filesByUploader = filesByUploader.mapKeys { it.key.toString() },
            averageFileSize = averageFileSize
        )
    }

    private fun mapRowToDto(row: ResultRow): FileAttachmentDto {
        return FileAttachmentDto(
            id = row[FileAttachments.id],
            relatedTable = row[FileAttachments.relatedTable],
            relatedId = row[FileAttachments.relatedId],
            fileName = row[FileAttachments.fileName],
            fileUrl = row[FileAttachments.fileUrl],
            fileSize = row[FileAttachments.fileSize],
            fileType = row[FileAttachments.fileType],
            uploadedBy = row[FileAttachments.uploadedBy],
            uploadedAt = row[FileAttachments.uploadedAt].toString(),
            uploadedByName = "${row.getOrNull(Users.firstName) ?: ""} ${row.getOrNull(Users.lastName) ?: ""}".trim(),
            uploadedByEmail = row.getOrNull(Users.email)
        )
    }
}