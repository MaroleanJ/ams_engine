package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class FileAttachmentDto(
    val id: Int? = null,
    val relatedTable: String,
    val relatedId: Int,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long? = null,
    val fileType: String? = null,
    val uploadedBy: Int,
    val uploadedAt: String? = null, // LocalDateTime as String
    // Additional fields for joined queries
    val uploadedByName: String? = null,
    val uploadedByEmail: String? = null
)

@Serializable
data class CreateFileAttachmentRequest(
    val relatedTable: String,
    val relatedId: Int,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long? = null,
    val fileType: String? = null,
    val uploadedBy: Int
)

@Serializable
data class UpdateFileAttachmentRequest(
    val relatedTable: String,
    val relatedId: Int,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long? = null,
    val fileType: String? = null,
    val uploadedBy: Int
)

@Serializable
data class BulkCreateFileAttachmentRequest(
    val relatedTable: String,
    val relatedId: Int,
    val uploadedBy: Int,
    val files: List<FileInfo>
)

@Serializable
data class FileInfo(
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long? = null,
    val fileType: String? = null
)

@Serializable
data class FileAttachmentsByEntityDto(
    val relatedTable: String,
    val relatedId: Int,
    val totalFiles: Int,
    val totalSize: Long? = null,
    val attachments: List<FileAttachmentDto>
)

@Serializable
data class FileAttachmentStatsDto(
    val totalFiles: Int,
    val totalSize: Long? = null,
    val filesByType: Map<String, Int>,
    val filesByTable: Map<String, Int>,
    val filesByUploader: Map<String, Int>,
    val averageFileSize: Long? = null
)