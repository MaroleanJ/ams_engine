package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.models.responses.ApiResponse
import com.techbros.services.AssetIssueService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.assetIssueRoutes(assetIssueService: AssetIssueService) {
    route("/api/v1/asset-issues") {

        // Get all asset issues
        get {
            val issues = assetIssueService.getAllAssetIssues()
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get issue statistics
        get("/stats") {
            val stats = assetIssueService.getIssueStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get asset issues by asset
        get("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByAsset(assetId)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issue history (with summary)
        get("/asset/{assetId}/history") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val history = assetIssueService.getAssetIssueHistory(assetId)
            call.respond(ApiResponse(
                success = true,
                data = history
            ))
        }

        // Get asset issues reported by user
        get("/reported-by/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByReportedBy(userId)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issues assigned to user
        get("/assigned-to/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByAssignedTo(userId)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get user issue statistics
        get("/user/{userId}/stats") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val stats = assetIssueService.getUserIssueStats(userId)
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get asset issues by type
        get("/type/{issueType}") {
            val issueType = call.parameters["issueType"]
                ?: throw ApiException("Issue type parameter is required", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByType(issueType)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issues by severity
        get("/severity/{severity}") {
            val severity = call.parameters["severity"]
                ?: throw ApiException("Severity parameter is required", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesBySeverity(severity)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issues by status
        get("/status/{status}") {
            val status = call.parameters["status"]
                ?: throw ApiException("Status parameter is required", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByStatus(status)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issues by date range
        get("/date-range") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("Start date parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("End date parameter is required", HttpStatusCode.BadRequest)

            val issues = assetIssueService.getAssetIssuesByDateRange(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = issues
            ))
        }

        // Get asset issue by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset issue ID", HttpStatusCode.BadRequest)

            val issue = assetIssueService.getAssetIssueById(id)
            call.respond(ApiResponse(
                success = true,
                data = issue
            ))
        }

        // Create asset issue
        post {
            try {
                val request = call.receive<CreateAssetIssueRequest>()
                val issue = assetIssueService.createAssetIssue(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = issue,
                        message = "Asset issue created successfully"
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

        // Bulk create asset issues
        post("/bulk") {
            try {
                val request = call.receive<BulkCreateAssetIssueRequest>()
                val issues = assetIssueService.bulkCreateAssetIssues(request)
                call.respond(
                    HttpStatusCode.Created,
                    ApiResponse(
                        success = true,
                        data = issues,
                        message = "Asset issues created successfully"
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

        // Update asset issue
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset issue ID", HttpStatusCode.BadRequest)

            try {
                val request = call.receive<UpdateAssetIssueRequest>()
                val issue = assetIssueService.updateAssetIssue(id, request)
                call.respond(ApiResponse(
                    success = true,
                    data = issue,
                    message = "Asset issue updated successfully"
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

        // Assign issue to user
        put("/{id}/assign") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset issue ID", HttpStatusCode.BadRequest)

            try {
                val request = call.receive<AssignIssueRequest>()
                val issue = assetIssueService.assignIssue(id, request)
                call.respond(ApiResponse(
                    success = true,
                    data = issue,
                    message = "Asset issue assigned successfully"
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

        // Resolve issue
        put("/{id}/resolve") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset issue ID", HttpStatusCode.BadRequest)

            try {
                val request = call.receive<ResolveIssueRequest>()
                val issue = assetIssueService.resolveIssue(id, request)
                call.respond(ApiResponse(
                    success = true,
                    data = issue,
                    message = "Asset issue resolved successfully"
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

        // Delete asset issue
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset issue ID", HttpStatusCode.BadRequest)

            assetIssueService.deleteAssetIssue(id)
            call.respond(ApiResponse<Unit>(
                success = true,
                message = "Asset issue deleted successfully"
            ))
        }

        // Delete all asset issues for a specific asset
        delete("/asset/{assetId}") {
            val assetId = call.parameters["assetId"]?.toIntOrNull()
                ?: throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)

            val deletedCount = assetIssueService.deleteAssetIssuesByAsset(assetId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "Asset issues deleted successfully"
            ))
        }
    }
}