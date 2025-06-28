package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.repositories.FileAttachmentRepository
import io.ktor.http.*

class FileAttachmentService(
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val userService: UserService
) {

    suspend fun createFileAttachment(request: CreateFileAttachmentRequest): FileAttachmentDto {
        validateFileAttachmentRequest(request)

        // Validate that user exists
        userService.getUserById(request.uploadedBy)

        val fileId = fileAttachmentRepository.create(request)
        return getFileAttachmentById(fileId)
    }

    suspend fun bulkCreateFileAttachments(request: BulkCreateFileAttachmentRequest): List<FileAttachmentDto> {
        validateRelatedTable(request.relatedTable)
        validateId(request.relatedId, "Related ID")
        validateId(request.uploadedBy, "Uploaded By")

        if (request.files.isEmpty()) {
            throw ApiException("Files list cannot be empty", HttpStatusCode.BadRequest)
        }

        // Validate that user exists
        userService.getUserById(request.uploadedBy)

        val createRequests = request.files.map { fileInfo ->
            validateFileName(fileInfo.fileName)
            validateFileUrl(fileInfo.fileUrl)

            CreateFileAttachmentRequest(
                relatedTable = request.relatedTable,
                relatedId = request.relatedId,
                fileName = fileInfo.fileName,
                fileUrl = fileInfo.fileUrl,
                fileSize = fileInfo.fileSize,
                fileType = fileInfo.fileType,
                uploadedBy = request.uploadedBy
            )
        }

        val fileIds = fileAttachmentRepository.bulkCreate(createRequests)
        return fileIds.map { getFileAttachmentById(it) }
    }

    suspend fun getFileAttachmentById(id: Int): FileAttachmentDto {
        validateId(id, "File Attachment ID")
        return fileAttachmentRepository.findById(id)
            ?: throw ApiException("File attachment not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllFileAttachments(): List<FileAttachmentDto> {
        return fileAttachmentRepository.findAll()
    }

    suspend fun updateFileAttachment(id: Int, request: UpdateFileAttachmentRequest): FileAttachmentDto {
        validateId(id, "File Attachment ID")
        validateFileAttachmentRequest(request)

        // Validate that user exists
        userService.getUserById(request.uploadedBy)

        val updated = fileAttachmentRepository.update(id, request)
        if (!updated) {
            throw ApiException("File attachment not found", HttpStatusCode.NotFound)
        }

        return getFileAttachmentById(id)
    }

    suspend fun deleteFileAttachment(id: Int) {
        validateId(id, "File Attachment ID")
        val deleted = fileAttachmentRepository.delete(id)
        if (!deleted) {
            throw ApiException("File attachment not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getFileAttachmentsByEntity(relatedTable: String, relatedId: Int): List<FileAttachmentDto> {
        validateRelatedTable(relatedTable)
        validateId(relatedId, "Related ID")
        return fileAttachmentRepository.findByEntity(relatedTable, relatedId)
    }

    suspend fun getFileAttachmentsByTable(relatedTable: String): List<FileAttachmentDto> {
        validateRelatedTable(relatedTable)
        return fileAttachmentRepository.findByTable(relatedTable)
    }

    suspend fun getFileAttachmentsByUploadedBy(uploadedBy: Int): List<FileAttachmentDto> {
        validateId(uploadedBy, "Uploaded By")
        // Validate user exists
        userService.getUserById(uploadedBy)
        return fileAttachmentRepository.findByUploadedBy(uploadedBy)
    }

    suspend fun getFileAttachmentsByType(fileType: String): List<FileAttachmentDto> {
        validateFileType(fileType)
        return fileAttachmentRepository.findByFileType(fileType)
    }

    suspend fun getFileAttachmentsByEntityWithSummary(relatedTable: String, relatedId: Int): FileAttachmentsByEntityDto {
        validateRelatedTable(relatedTable)
        validateId(relatedId, "Related ID")
        return fileAttachmentRepository.getFileAttachmentsByEntity(relatedTable, relatedId)
    }

    suspend fun getFileAttachmentStats(): FileAttachmentStatsDto {
        return fileAttachmentRepository.getFileAttachmentStats()
    }

    suspend fun deleteFileAttachmentsByEntity(relatedTable: String, relatedId: Int): Int {
        validateRelatedTable(relatedTable)
        validateId(relatedId, "Related ID")
        return fileAttachmentRepository.deleteByEntity(relatedTable, relatedId)
    }

    suspend fun deleteFileAttachmentsByTable(relatedTable: String): Int {
        validateRelatedTable(relatedTable)
        return fileAttachmentRepository.deleteByTable(relatedTable)
    }

    suspend fun deleteFileAttachmentsByUploadedBy(uploadedBy: Int): Int {
        validateId(uploadedBy, "Uploaded By")
        // Validate user exists
        userService.getUserById(uploadedBy)
        return fileAttachmentRepository.deleteByUploadedBy(uploadedBy)
    }

    private fun validateFileAttachmentRequest(request: CreateFileAttachmentRequest) {
        validateRelatedTable(request.relatedTable)
        validateId(request.relatedId, "Related ID")
        validateFileName(request.fileName)
        validateFileUrl(request.fileUrl)
        validateId(request.uploadedBy, "Uploaded By")

        if (request.fileSize != null && request.fileSize < 0) {
            throw ApiException("File size cannot be negative", HttpStatusCode.BadRequest)
        }
    }

    private fun validateFileAttachmentRequest(request: UpdateFileAttachmentRequest) {
        validateRelatedTable(request.relatedTable)
        validateId(request.relatedId, "Related ID")
        validateFileName(request.fileName)
        validateFileUrl(request.fileUrl)
        validateId(request.uploadedBy, "Uploaded By")

        if (request.fileSize != null && request.fileSize < 0) {
            throw ApiException("File size cannot be negative", HttpStatusCode.BadRequest)
        }
    }

    private fun validateId(id: Int, fieldName: String) {
        if (id <= 0) {
            throw ApiException("$fieldName must be a positive integer", HttpStatusCode.BadRequest)
        }
    }

    private fun validateRelatedTable(relatedTable: String) {
        if (relatedTable.isBlank()) {
            throw ApiException("Related table cannot be empty", HttpStatusCode.BadRequest)
        }
        if (relatedTable.length > 100) {
            throw ApiException("Related table cannot exceed 100 characters", HttpStatusCode.BadRequest)
        }

        // Validate against allowed table names
        val allowedTables = listOf(
            "assets", "maintenance_records", "maintenance_schedules", "asset_issues",
            "software_licenses", "subscriptions", "users", "vendors", "locations"
        )
        if (relatedTable !in allowedTables) {
            throw ApiException("Invalid related table. Allowed tables: ${allowedTables.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }

    private fun validateFileName(fileName: String) {
        if (fileName.isBlank()) {
            throw ApiException("File name cannot be empty", HttpStatusCode.BadRequest)
        }
        if (fileName.length > 255) {
            throw ApiException("File name cannot exceed 255 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateFileUrl(fileUrl: String) {
        if (fileUrl.isBlank()) {
            throw ApiException("File URL cannot be empty", HttpStatusCode.BadRequest)
        }
        // Basic URL validation
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://") && !fileUrl.startsWith("/")) {
            throw ApiException("File URL must be a valid URL or path", HttpStatusCode.BadRequest)
        }
    }

    private fun validateFileType(fileType: String) {
        if (fileType.isBlank()) {
            throw ApiException("File type cannot be empty", HttpStatusCode.BadRequest)
        }
        if (fileType.length > 100) {
            throw ApiException("File type cannot exceed 100 characters", HttpStatusCode.BadRequest)
        }
    }
}