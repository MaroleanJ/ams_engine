package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateLocationRequest
import com.techbros.models.dto.UpdateLocationRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.LocationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.locationRoutes(locationService: LocationService) {
    route("/api/v1/locations") {

        // Get all locations
        get {
            val locations = locationService.getAllLocations()
            call.respond(ApiResponse(
                success = true,
                data = locations
                )
            )
        }

        // Get location hierarchy
        get("/hierarchy") {
            val hierarchy = locationService.getLocationHierarchy()
            call.respond(ApiResponse(
                success = true,
                data = hierarchy
            ))
        }

        // Get root locations (no parent)
        get("/roots") {
            val locations = locationService.getRootLocations()
            call.respond(ApiResponse(
                success = true,
                data = locations
            ))
        }

        // Get locations by parent ID
        get("/parent/{parentId?}") {
            val parentIdParam = call.parameters["parentId"]
            val parentId = when {
                parentIdParam == null || parentIdParam == "null" -> null
                else -> parentIdParam.toIntOrNull()
                    ?: throw ApiException("Invalid parent ID", HttpStatusCode.BadRequest)
            }

            val locations = locationService.getLocationsByParent(parentId)
            call.respond(ApiResponse(
                success = true,
                data = locations
            ))
        }

        // Search locations by name
        get("/search") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val locations = locationService.searchLocations(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = locations
            ))
        }

        // Get location by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid location ID", HttpStatusCode.BadRequest)

            val location = locationService.getLocationById(id)
            call.respond(ApiResponse(
                success = true,
                data = location
            ))
        }

        // Create location
        post {
            val request = call.receive<CreateLocationRequest>()
            val location = locationService.createLocation(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = location,
                message = "Location created successfully"
            ))
        }

        // Update location
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid location ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateLocationRequest>()
            val location = locationService.updateLocation(id, request)
            call.respond(ApiResponse(
                success = true,
                data = location,
                message = "Location updated successfully"
            ))
        }

        // Delete location
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid location ID", HttpStatusCode.BadRequest)

            locationService.deleteLocation(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Location deleted successfully"
            ))
        }
    }
}