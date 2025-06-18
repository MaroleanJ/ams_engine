package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateAssetCategoryRequest
import com.techbros.models.dto.UpdateAssetCategoryRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.AssetCategoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.assetCategoryRoutes(assetCategoryService: AssetCategoryService) {
    route("/api/v1/asset-categories") {

        // Get all asset categories
        get {
            val categories = assetCategoryService.getAllAssetCategories()
            call.respond(
                ApiResponse(
                success = true,
                data = categories
            )
            )
        }

        // Get asset categories count
        get("/count") {
            val count = assetCategoryService.getAssetCategoryCount()
            call.respond(ApiResponse(
                success = true,
                data = mapOf("count" to count)
            ))
        }

        // Search asset categories by name
        get("/search") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val categories = assetCategoryService.searchAssetCategories(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = categories
            ))
        }

        // Get asset category by name
        get("/name/{name}") {
            val name = call.parameters["name"]
                ?: throw ApiException("Category name is required", HttpStatusCode.BadRequest)

            val category = assetCategoryService.getAssetCategoryByName(name)
            call.respond(ApiResponse(
                success = true,
                data = category
            ))
        }

        // Get asset category by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset category ID", HttpStatusCode.BadRequest)

            val category = assetCategoryService.getAssetCategoryById(id)
            call.respond(ApiResponse(
                success = true,
                data = category
            ))
        }

        // Create asset category
        post {
            val request = call.receive<CreateAssetCategoryRequest>()
            val category = assetCategoryService.createAssetCategory(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = category,
                message = "Asset category created successfully"
            ))
        }

        // Update asset category
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset category ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateAssetCategoryRequest>()
            val category = assetCategoryService.updateAssetCategory(id, request)
            call.respond(ApiResponse(
                success = true,
                data = category,
                message = "Asset category updated successfully"
            ))
        }

        // Delete asset category
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset category ID", HttpStatusCode.BadRequest)

            assetCategoryService.deleteAssetCategory(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Asset category deleted successfully"
            ))
        }

        // Validate category exists (utility endpoint)
        head("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset category ID", HttpStatusCode.BadRequest)

            assetCategoryService.validateCategoryExists(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}