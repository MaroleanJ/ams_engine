package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateMaintenanceScheduleRequest
import com.techbros.models.dto.UpdateMaintenanceScheduleRequest
import com.techbros.services.MaintenanceScheduleService
import com.techbros.models.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.maintenanceScheduleRoutes(maintenanceScheduleService: MaintenanceScheduleService) {
    route("/api/v1/maintenance-schedules") {

        // Get all maintenance schedules
        get {
            try {
                val schedules = maintenanceScheduleService.getAllMaintenanceSchedules()
                call.respond(
                    ApiResponse(
                        success = true,
                        data = schedules,
                        message = "Maintenance schedules fetched successfully"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch maintenance schedules: ${e.message}"
                    )
                )
            }
        }

        // Get maintenance schedule summary
        get("/summary") {
            val summary = maintenanceScheduleService.getMaintenanceScheduleSummary()
            call.respond(ApiResponse(
                success = true,
                data = summary
            ))
        }

        // Get active maintenance schedules
        get("/active") {
            val schedules = maintenanceScheduleService.getActiveMaintenanceSchedules()
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get overdue maintenance schedules
        get("/overdue") {
            val schedules = maintenanceScheduleService.getOverdueMaintenanceSchedules()
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules due today
        get("/due-today") {
            val schedules = maintenanceScheduleService.getMaintenanceSchedulesDueToday()
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules due this week
        get("/due-this-week") {
            val schedules = maintenanceScheduleService.getMaintenanceSchedulesDueThisWeek()
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules by date range
        get("/due-range") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("startDate parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("endDate parameter is required", HttpStatusCode.BadRequest)

            val schedules = maintenanceScheduleService.getMaintenanceSchedulesDueInRange(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules by asset ID
        get("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val schedules = maintenanceScheduleService.getMaintenanceSchedulesByAssetId(assetId)
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules by assigned user
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val schedules = maintenanceScheduleService.getMaintenanceSchedulesByAssignedUser(userId)
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedules by priority
        get("/priority/{priority}") {
            val priority = call.parameters["priority"]
                ?: throw ApiException("Priority parameter is required", HttpStatusCode.BadRequest)

            val schedules = maintenanceScheduleService.getMaintenanceSchedulesByPriority(priority)
            call.respond(ApiResponse(
                success = true,
                data = schedules
            ))
        }

        // Get maintenance schedule by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance schedule ID", HttpStatusCode.BadRequest)

            val schedule = maintenanceScheduleService.getMaintenanceScheduleById(id)
            call.respond(ApiResponse(
                success = true,
                data = schedule
            ))
        }

        // Create maintenance schedule
        post {
            val request = call.receive<CreateMaintenanceScheduleRequest>()
            val schedule = maintenanceScheduleService.createMaintenanceSchedule(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = schedule,
                message = "Maintenance schedule created successfully"
            ))
        }

        // Update maintenance schedule
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance schedule ID", HttpStatusCode.BadRequest)
            
            val request = call.receive<UpdateMaintenanceScheduleRequest>()
            val schedule = maintenanceScheduleService.updateMaintenanceSchedule(id, request)
            call.respond(ApiResponse(
                success = true,
                data = schedule,
                message = "Maintenance schedule updated successfully"
            ))
        }

        // Complete maintenance schedule (mark as completed and calculate next due date)
        patch("/{id}/complete") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance schedule ID", HttpStatusCode.BadRequest)
            
            val completedDate = call.request.queryParameters["completedDate"]
                ?: throw ApiException("completedDate parameter is required", HttpStatusCode.BadRequest)
            
            val schedule = maintenanceScheduleService.completeMaintenanceSchedule(id, completedDate)
            call.respond(ApiResponse(
                success = true,
                data = schedule,
                message = "Maintenance schedule completed successfully"
            ))
        }

        // Deactivate maintenance schedule
        patch("/{id}/deactivate") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance schedule ID", HttpStatusCode.BadRequest)
            
            val schedule = maintenanceScheduleService.deactivateMaintenanceSchedule(id)
            call.respond(ApiResponse(
                success = true,
                data = schedule,
                message = "Maintenance schedule deactivated successfully"
            ))
        }

        // Delete maintenance schedule
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance schedule ID", HttpStatusCode.BadRequest)
            
            maintenanceScheduleService.deleteMaintenanceSchedule(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}