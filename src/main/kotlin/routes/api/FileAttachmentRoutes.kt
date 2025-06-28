package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateFileAttachmentRequest
import com.techbros.models.dto.UpdateFileAttachmentRequest
import com.techbros.models.dto.BulkCreateFileAttachmentRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.FileAttachmentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.fileAttachmentRoutes(fileAttachmentService: FileAttachmentService) {
    route("/api/v1/file-attachments") {

        // Get all file attachments
        get {
            val attachments = fileAttachmentService.getAllFileAttachments()
            call.respond(ApiResponse(
                success = true,
                data = attachments
            ))
        }

        // Get file attachment statistics
        get("/stats") {
            val stats = fileAttachmentService.getFileAttachmentStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get file attachments by entity (table and ID)
        get("/entity/{relatedTable}/{relatedId}") {
            val relatedTable = call.parameters["relatedTable"]
                ?: throw ApiException("Related table parameter is required", HttpStatusCode.BadRequest)
            val relatedId = call.parameters["relatedId"]?.toIntOrNull()
                ?: throw ApiException("Invalid related ID", HttpStatusCode.BadRequest)

            val attachments = fileAttachmentService.getFileAttachmentsByEntity(relatedTable, relatedId)
            call.respond(ApiResponse(
                success = true,
                data = attachments
            ))
        }

        // Get file attachments by entity with summary
        get("/entity/{relatedTable}/{relatedId}/summary") {
            val relatedTable = call.parameters["relatedTable"]
                ?: throw ApiException("Related table parameter is required", HttpStatusCode.BadRequest)
            val relatedId = call.parameters["relatedId"]?.toIntOrNull()
                ?: throw ApiException("Invalid related ID", HttpStatusCode.BadRequest)

            val summary = fileAttachmentService.getFileAttachmentsByEntityWithSummary(relatedTable, relatedId)
            call.respond(ApiResponse(
                success = true,
                data = summary
            ))
        }

        // Get file attachments by table
        get("/table/{relatedTable}") {
            val relatedTable = call.parameters["relatedTable"]
                ?: throw ApiException("Related table parameter is required", HttpStatusCode.BadRequest)

            val attachments = fileAttachmentService.getFileAttachmentsByTable(relatedTable)
            call.respond(ApiResponse(
                success = true,
                data = attachments
            ))
        }

        // Get file attachments by uploader
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val attachments = fileAttachmentService.getFileAttachmentsByUploadedBy(userId)
            call.respond(ApiResponse(
                success = true,
                data = attachments
            ))
        }

        // Get file attachments by file type
        get("/type/{fileType}") {
            val fileType = call.parameters["fileType"]
                ?: throw ApiException("File type parameter is required", HttpStatusCode.BadRequest)

            val attachments = fileAttachmentService.getFileAttachmentsByType(fileType)
            call.respond(ApiResponse(
                success = true,
                data = attachments
            ))
        }

        // Get file attachment by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid file attachment ID", HttpStatusCode.BadRequest)

            val attachment = fileAttachmentService.getFileAttachmentById(id)
            call.respond(ApiResponse(
                success = true,
                data = attachment
            ))
        }

        // Create file attachment
        post {
            try {
                val request = call.receive<CreateFileAttachmentRequest>()
                val attachment = fileAttachmentService.createFileAttachment(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = attachment,
                        message = "File attachment created successfully"
                    )
                )
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "An unexpected error occurred: ${e.message}"
                    )
                )
            }
        }

        // Bulk create file attachments
        post("/bulk") {
            try {
                val request = call.receive<BulkCreateFileAttachmentRequest>()
                val attachments = fileAttachmentService.bulkCreateFileAttachments(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = attachments,
                        message = "File attachments created successfully"
                    )
                )
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "An unexpected error occurred: ${e.message}"
                    )
                )
            }
        }

        // Update file attachment
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid file attachment ID", HttpStatusCode.BadRequest)

            try {
                val request = call.receive<UpdateFileAttachmentRequest>()
                val attachment = fileAttachmentService.updateFileAttachment(id, request)
                call.respond(ApiResponse(
                    success = true,
                    data = attachment,
                    message = "File attachment updated successfully"
                ))
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "An unexpected error occurred: ${e.message}"
                    )
                )
            }
        }

        // Delete file attachment
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid file attachment ID", HttpStatusCode.BadRequest)

            fileAttachmentService.deleteFileAttachment(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "File attachment deleted successfully"
            ))
        }

        // Delete file attachments by entity
        delete("/entity/{relatedTable}/{relatedId}") {
            val relatedTable = call.parameters["relatedTable"]
                ?: throw ApiException("Related table parameter is required", HttpStatusCode.BadRequest)
            val relatedId = call.parameters["relatedId"]?.toIntOrNull()
                ?: throw ApiException("Invalid related ID", HttpStatusCode.BadRequest)

            val deletedCount = fileAttachmentService.deleteFileAttachmentsByEntity(relatedTable, relatedId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "File attachments deleted successfully"
            ))
        }

        // Delete file attachments by table
        delete("/table/{relatedTable}") {
            val relatedTable = call.parameters["relatedTable"]
                ?: throw ApiException("Related table parameter is required", HttpStatusCode.BadRequest)

            val deletedCount = fileAttachmentService.deleteFileAttachmentsByTable(relatedTable)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "File attachments deleted successfully"
            ))
        }

        // Delete file attachments by uploader
        delete("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val deletedCount = fileAttachmentService.deleteFileAttachmentsByUploadedBy(userId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "File attachments deleted successfully"
            ))
        }
    }
}