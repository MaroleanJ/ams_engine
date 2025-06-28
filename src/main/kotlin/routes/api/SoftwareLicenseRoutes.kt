package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateSoftwareLicenseRequest
import com.techbros.models.dto.UpdateSoftwareLicenseRequest
import com.techbros.models.dto.SeatUsageRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.SoftwareLicenseService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.softwareLicenseRoutes(softwareLicenseService: SoftwareLicenseService) {
    route("/api/v1/software-licenses") {

        // Get all software licenses
        get {
            val licenses = softwareLicenseService.getAllSoftwareLicenses()
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software license statistics
        get("/stats") {
            val stats = softwareLicenseService.getSoftwareLicenseStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get expired software licenses
        get("/expired") {
            val licenses = softwareLicenseService.getExpiredSoftwareLicenses()
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software licenses expiring in X days
        get("/expiring") {
            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 30
            val licenses = softwareLicenseService.getSoftwareLicensesExpiringInDays(days)
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software licenses by vendor
        get("/vendor/{vendorId}") {
            val vendorId = call.parameters["vendorId"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val licenses = softwareLicenseService.getSoftwareLicensesByVendor(vendorId)
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software licenses by assigned user
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val licenses = softwareLicenseService.getSoftwareLicensesByAssignedTo(userId)
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software licenses by status
        get("/status/{status}") {
            val status = call.parameters["status"]
                ?: throw ApiException("Status parameter is required", HttpStatusCode.BadRequest)

            val licenses = softwareLicenseService.getSoftwareLicensesByStatus(status)
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Get software licenses by date range
        get("/date-range") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("Start date parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("End date parameter is required", HttpStatusCode.BadRequest)

            val licenses = softwareLicenseService.getSoftwareLicensesByDateRange(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = licenses
            ))
        }

        // Create a new software license
        post {
            val request = call.receive<CreateSoftwareLicenseRequest>()
            val license = softwareLicenseService.createSoftwareLicense(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = license,
                message = "Software license created successfully"
            ))
        }

        // Get software license by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid software license ID", HttpStatusCode.BadRequest)

            val license = softwareLicenseService.getSoftwareLicenseById(id)
            call.respond(ApiResponse(
                success = true,
                data = license
            ))
        }

        // Update software license
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid software license ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateSoftwareLicenseRequest>()
            val license = softwareLicenseService.updateSoftwareLicense(id, request)
            call.respond(ApiResponse(
                success = true,
                data = license,
                message = "Software license updated successfully"
            ))
        }

        // Delete software license
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid software license ID", HttpStatusCode.BadRequest)

            softwareLicenseService.deleteSoftwareLicense(id)
            call.respond(
                ApiResponse<Unit>(
                success = true,
                message = "Software license deleted successfully"
            )
            )
        }

        // Update seat usage for a software license
        patch("/{id}/seats") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid software license ID", HttpStatusCode.BadRequest)

            val request = call.receive<SeatUsageRequest>()
            val license = softwareLicenseService.updateSeatUsage(id, request)
            call.respond(ApiResponse(
                success = true,
                data = license,
                message = "Seat usage updated successfully"
            ))
        }

        // Delete all software licenses for a vendor
        delete("/vendor/{vendorId}") {
            val vendorId = call.parameters["vendorId"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val deletedCount = softwareLicenseService.deleteSoftwareLicensesByVendor(vendorId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "Software licenses deleted successfully"
            ))
        }
    }
}