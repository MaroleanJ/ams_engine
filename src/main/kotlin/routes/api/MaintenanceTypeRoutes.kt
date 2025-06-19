package com.techbros.routes.api

import com.techbros.models.dto.CreateMaintenanceTypeRequest
import com.techbros.models.dto.UpdateMaintenanceTypeRequest
import com.techbros.services.MaintenanceTypeService
import com.techbros.exceptions.ApiException
import com.techbros.models.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.maintenanceTypeRoutes(maintenanceTypeService: MaintenanceTypeService) {
    route("/api/v1/maintenance-types") {

        // Get all maintenance types
        get {
            val maintenanceTypes = maintenanceTypeService.getAllMaintenanceTypes()
            call.respond(
                ApiResponse(
                success = true,
                data = maintenanceTypes
            )
            )
        }

        // Search maintenance types by name
        get("/search") {
            val namePattern = call.request.queryParameters["name"]
                ?: throw ApiException("name parameter is required", HttpStatusCode.BadRequest)

            val maintenanceTypes = maintenanceTypeService.searchMaintenanceTypesByName(namePattern)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceTypes
            ))
        }

        // Get maintenance types by duration range
        get("/duration-range") {
            val minHours = call.request.queryParameters["minHours"]?.toIntOrNull()
            val maxHours = call.request.queryParameters["maxHours"]?.toIntOrNull()

            val maintenanceTypes = maintenanceTypeService.getMaintenanceTypesByDurationRange(minHours, maxHours)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceTypes
            ))
        }

        // Get maintenance types by cost range
        get("/cost-range") {
            val minCost = call.request.queryParameters["minCost"]
            val maxCost = call.request.queryParameters["maxCost"]

            val maintenanceTypes = maintenanceTypeService.getMaintenanceTypesByCostRange(minCost, maxCost)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceTypes
            ))
        }

        // Get maintenance type by name
        get("/name/{name}") {
            val name = call.parameters["name"]
                ?: throw ApiException("Maintenance type name is required", HttpStatusCode.BadRequest)

            val maintenanceType = maintenanceTypeService.getMaintenanceTypeByName(name)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceType
            ))
        }

        // Get maintenance type by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance type ID", HttpStatusCode.BadRequest)

            val maintenanceType = maintenanceTypeService.getMaintenanceTypeById(id)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceType
            ))
        }

        // Create maintenance type
        post {
            val request = call.receive<CreateMaintenanceTypeRequest>()
            val maintenanceType = maintenanceTypeService.createMaintenanceType(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = maintenanceType,
                message = "Maintenance type created successfully"
            ))
        }

        // Update maintenance type
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance type ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateMaintenanceTypeRequest>()
            val maintenanceType = maintenanceTypeService.updateMaintenanceType(id, request)
            call.respond(ApiResponse(
                success = true,
                data = maintenanceType,
                message = "Maintenance type updated successfully"
            ))
        }

        // Delete maintenance type
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid maintenance type ID", HttpStatusCode.BadRequest)

            maintenanceTypeService.deleteMaintenanceType(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Maintenance type deleted successfully"
            ))
        }
    }
}