package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateMaintenanceRecordRequest
import com.techbros.models.dto.UpdateMaintenanceRecordRequest
import com.techbros.models.dto.BulkCreateMaintenanceRecordRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.MaintenanceRecordService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.maintenanceRecordRoutes(maintenanceRecordService: MaintenanceRecordService) {
    route("/api/v1/maintenance-records") {

        // Get all maintenance records
        get {
            val records = maintenanceRecordService.getAllMaintenanceRecords()
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance statistics
        get("/stats") {
            val stats = maintenanceRecordService.getMaintenanceStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get maintenance records by asset
        get("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsByAsset(assetId)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get asset maintenance history (with summary)
        get("/asset/{assetId}/history") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val history = maintenanceRecordService.getAssetMaintenanceHistory(assetId)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get maintenance records by performed by user
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsByPerformedBy(userId)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance records by schedule
        get("/schedule/{scheduleId}") {
            val scheduleId = call.parameters["scheduleId"]?.toIntOrNull()
                ?: throw ApiException("Invalid schedule ID", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsBySchedule(scheduleId)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance records by type
        get("/type/{maintenanceType}") {
            val maintenanceType = call.parameters["maintenanceType"]
                ?: throw ApiException("Maintenance type parameter is required", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsByType(maintenanceType)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance records by status
        get("/status/{status}") {
            val status = call.parameters["status"]
                ?: throw ApiException("Status parameter is required", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsByStatus(status)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance records by date range
        get("/date-range") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("Start date parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("End date parameter is required", HttpStatusCode.BadRequest)

            val records = maintenanceRecordService.getMaintenanceRecordsByDateRange(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = records
            ))
        }

        // Get maintenance record by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance record ID", HttpStatusCode.BadRequest)

            val record = maintenanceRecordService.getMaintenanceRecordById(id)
            call.respond(ApiResponse(
                success = true,
                data = record
            ))
        }

        // Create maintenance record
        post {
            try {
                val request = call.receive<CreateMaintenanceRecordRequest>()
                val record = maintenanceRecordService.createMaintenanceRecord(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = record,
                        message = "Maintenance record created successfully"
                    )
                )
            } catch (e: ContentTransformationException) {
                // Handles invalid/malformed JSON input
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                // Handles unexpected errors from the service or elsewhere
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

        // Bulk create maintenance records
        post("/bulk") {
            try {
                val request = call.receive<BulkCreateMaintenanceRecordRequest>()
                val records = maintenanceRecordService.bulkCreateMaintenanceRecords(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = records,
                        message = "Maintenance records created successfully"
                    )
                )
            } catch (e: ContentTransformationException) {
                // Handles invalid/malformed JSON input
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                // Handles unexpected errors from the service or elsewhere
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

        // Update maintenance record
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance record ID", HttpStatusCode.BadRequest)

            try {
                val request = call.receive<UpdateMaintenanceRecordRequest>()
                val record = maintenanceRecordService.updateMaintenanceRecord(id, request)
                call.respond(ApiResponse(
                    success = true,
                    data = record,
                    message = "Maintenance record updated successfully"
                ))
            } catch (e: ContentTransformationException) {
                // Handles invalid/malformed JSON input
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid request format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                // Handles unexpected errors from the service or elsewhere
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

        // Delete maintenance record
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance record ID", HttpStatusCode.BadRequest)

            maintenanceRecordService.deleteMaintenanceRecord(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Maintenance record deleted successfully"
            ))
        }

        // Delete maintenance records by asset
        delete("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val deletedCount = maintenanceRecordService.deleteMaintenanceRecordsByAsset(assetId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "Maintenance records deleted successfully"
            ))
        }

        // Delete maintenance records by schedule
        delete("/schedule/{scheduleId}") {
            val scheduleId = call.parameters["scheduleId"]?.toIntOrNull()
                ?: throw ApiException("Invalid schedule ID", HttpStatusCode.BadRequest)

            val deletedCount = maintenanceRecordService.deleteMaintenanceRecordsBySchedule(scheduleId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "Maintenance records deleted successfully"
            ))
        }
    }
}