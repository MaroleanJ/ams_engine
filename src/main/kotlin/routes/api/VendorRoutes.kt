package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateVendorRequest
import com.techbros.models.dto.UpdateVendorRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.VendorService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vendorRoutes(vendorService: VendorService) {
    route("/api/v1/vendors") {

        // Get all vendors
        get {
            val vendors = vendorService.getAllVendors()
            call.respond(
                ApiResponse(
                success = true,
                data = vendors
            )
            )
        }

        // Get all vendors summary
        get("/summary") {
            val vendors = vendorService.getAllVendorsSummary()
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Get vendor statistics
        get("/stats") {
            val stats = vendorService.getVendorStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get vendors with email
        get("/with-email") {
            val vendors = vendorService.getVendorsWithEmail()
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Get vendors with phone
        get("/with-phone") {
            val vendors = vendorService.getVendorsWithPhone()
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Search vendors by name
        get("/search") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val vendors = vendorService.searchVendors(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Search vendors by contact (name, email, or phone)
        get("/search/contact") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val vendors = vendorService.searchVendorsByContact(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Get vendors by email
        get("/email/{email}") {
            val email = call.parameters["email"]
                ?: throw ApiException("Email is required", HttpStatusCode.BadRequest)

            val vendors = vendorService.getVendorsByEmail(email)
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Get vendors by phone
        get("/phone/{phone}") {
            val phone = call.parameters["phone"]
                ?: throw ApiException("Phone is required", HttpStatusCode.BadRequest)

            val vendors = vendorService.getVendorsByPhone(phone)
            call.respond(ApiResponse(
                success = true,
                data = vendors
            ))
        }

        // Get vendor by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val vendor = vendorService.getVendorById(id)
            call.respond(ApiResponse(
                success = true,
                data = vendor
            ))
        }

        // Create vendor
        post {
            val request = call.receive<CreateVendorRequest>()
            val vendor = vendorService.createVendor(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = vendor,
                message = "Vendor created successfully"
            ))
        }

        // Update vendor
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateVendorRequest>()
            val vendor = vendorService.updateVendor(id, request)
            call.respond(ApiResponse(
                success = true,
                data = vendor,
                message = "Vendor updated successfully"
            ))
        }

        // Delete vendor
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            vendorService.deleteVendor(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Vendor deleted successfully"
            ))
        }

        // Validate vendor exists (utility endpoint)
        head("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            vendorService.validateVendorExists(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}