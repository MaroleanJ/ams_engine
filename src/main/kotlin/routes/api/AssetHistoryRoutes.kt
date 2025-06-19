package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateAssetHistoryRequest
import com.techbros.models.dto.UpdateAssetHistoryRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.AssetHistoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.assetHistoryRoutes(assetHistoryService: AssetHistoryService) {
    route("/api/v1/asset-history") {

        // Get all asset history records
        get {
            val history = assetHistoryService.getAllAssetHistory()
            call.respond(
                ApiResponse(
                success = true,
                data = history
            )
            )
        }

        // Get asset history by asset ID
        get("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val history = assetHistoryService.getAssetHistoryByAssetId(assetId)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get asset history by user ID
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val history = assetHistoryService.getAssetHistoryByUser(userId)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get asset history by action type
        get("/action/{actionType}") {
            val actionType = call.parameters["actionType"]
                ?: throw ApiException("Action type parameter is required", HttpStatusCode.BadRequest)

            val history = assetHistoryService.getAssetHistoryByActionType(actionType)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get asset history by date range
        get("/range") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("startDate parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("endDate parameter is required", HttpStatusCode.BadRequest)

            val history = assetHistoryService.getAssetHistoryByDateRange(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get asset history record by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid history record ID", HttpStatusCode.BadRequest)

            val history = assetHistoryService.getAssetHistoryById(id)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Create asset history record
        post {
            val request = call.receive<CreateAssetHistoryRequest>()
            val history = assetHistoryService.createAssetHistory(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = history,
                message = "Asset history record created successfully"
            ))
        }

        // Update asset history record
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid history record ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateAssetHistoryRequest>()
            val history = assetHistoryService.updateAssetHistory(id, request)
            call.respond(ApiResponse(
                success = true,
                data = history,
                message = "Asset history record updated successfully"
            ))
        }

        // Delete asset history record
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid history record ID", HttpStatusCode.BadRequest)

            assetHistoryService.deleteAssetHistory(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Asset history record deleted successfully"
            ))
        }

        // Delete all asset history records for a specific asset
        delete("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val deletedCount = assetHistoryService.deleteAssetHistoryByAssetId(assetId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "Asset history records deleted successfully"
            ))
        }
    }
}