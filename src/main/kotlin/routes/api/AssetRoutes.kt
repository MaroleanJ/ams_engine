package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateAssetRequest
import com.techbros.models.dto.UpdateAssetRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.AssetService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.assetRoutes(assetService: AssetService) {
    route("/api/v1/assets") {

        // Get all assets
        get {
            val assets = assetService.getAllAssets()
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets summary
        get("/summary") {
            val assetsSummary = assetService.getAllAssetsSummary()
            call.respond(ApiResponse(
                success = true,
                data = assetsSummary
            ))
        }

        // Get asset statistics
        get("/stats") {
            val stats = assetService.getAssetStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get unassigned assets
        get("/unassigned") {
            val assets = assetService.getUnassignedAssets()
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets with warranty expiring
        get("/warranty-expiring") {
            val days = call.request.queryParameters["days"]?.toLongOrNull() ?: 30L
            val assets = assetService.getAssetsWithWarrantyExpiring(days)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Search assets by name
        get("/search") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val assets = assetService.searchAssets(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Search assets by name or model
        get("/search/name-model") {
            val searchTerm = call.request.queryParameters["q"]
                ?: throw ApiException("Search term 'q' parameter is required", HttpStatusCode.BadRequest)

            val assets = assetService.searchAssetsByNameOrModel(searchTerm)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets by category
        get("/category/{categoryId}") {
            val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                ?: throw ApiException("Invalid category ID", HttpStatusCode.BadRequest)

            val assets = assetService.getAssetsByCategory(categoryId)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets by vendor
        get("/vendor/{vendorId}") {
            val vendorId = call.parameters["vendorId"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val assets = assetService.getAssetsByVendor(vendorId)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets by location
        get("/location/{locationId}") {
            val locationId = call.parameters["locationId"]?.toIntOrNull()
                ?: throw ApiException("Invalid location ID", HttpStatusCode.BadRequest)

            val assets = assetService.getAssetsByLocation(locationId)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets by assigned user
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val assets = assetService.getAssetsByAssignedUser(userId)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get assets by status
        get("/status/{status}") {
            val status = call.parameters["status"]
                ?: throw ApiException("Status parameter is required", HttpStatusCode.BadRequest)

            val assets = assetService.getAssetsByStatus(status)
            call.respond(ApiResponse(
                success = true,
                data = assets
            ))
        }

        // Get asset by serial number
        get("/serial/{serialNumber}") {
            val serialNumber = call.parameters["serialNumber"]
                ?: throw ApiException("Serial number parameter is required", HttpStatusCode.BadRequest)

            val asset = assetService.getAssetBySerialNumber(serialNumber)
            call.respond(ApiResponse(
                success = true,
                data = asset
            ))
        }

        // Get asset by barcode
        get("/barcode/{barcode}") {
            val barcode = call.parameters["barcode"]
                ?: throw ApiException("Barcode parameter is required", HttpStatusCode.BadRequest)

            val asset = assetService.getAssetByBarcode(barcode)
            call.respond(ApiResponse(
                success = true,
                data = asset
            ))
        }

        // Get asset by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val asset = assetService.getAssetById(id)
            call.respond(ApiResponse(
                success = true,
                data = asset
            ))
        }

        // Get detailed asset by ID
        get("/{id}/detail") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val asset = assetService.getAssetDetailById(id)
            call.respond(ApiResponse(
                success = true,
                data = asset
            ))
        }

        // Create asset
        post {
            val request = call.receive<CreateAssetRequest>()
            val asset = assetService.createAsset(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = asset,
                message = "Asset created successfully"
            ))
        }

        // Update asset
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateAssetRequest>()
            val asset = assetService.updateAsset(id, request)
            call.respond(ApiResponse(
                success = true,
                data = asset,
                message = "Asset updated successfully"
            ))
        }

        // Delete asset
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            assetService.deleteAsset(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Asset deleted successfully"
            ))
        }

        // Validate asset exists (utility endpoint)
        head("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            assetService.validateAssetExists(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}