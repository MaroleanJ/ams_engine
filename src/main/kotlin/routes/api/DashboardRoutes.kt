package com.techbros.routes.api

import com.techbros.models.dto.DashboardFiltersDto
import com.techbros.models.dto.DateRangeDto
import com.techbros.models.responses.ApiResponse
import com.techbros.services.DashboardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRoutes(dashboardService: DashboardService) {
    route("/api/v1/dashboard") {

        // Get complete dashboard overview
        get("/overview") {
            try {
                val filters = call.request.queryParameters.let { params ->
                    val locationId = params["locationId"]?.toIntOrNull()
                    val categoryId = params["categoryId"]?.toIntOrNull()
                    val startDate = params["startDate"]
                    val endDate = params["endDate"]

                    if (locationId != null || categoryId != null || (startDate != null && endDate != null)) {
                        DashboardFiltersDto(
                            locationId = locationId,
                            categoryId = categoryId,
                            dateRange = if (startDate != null && endDate != null) {
                                DateRangeDto(startDate, endDate)
                            } else null
                        )
                    } else null
                }

                val overview = dashboardService.getDashboardOverview(filters)
                call.respond(ApiResponse(
                    success = true,
                    data = overview
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch dashboard overview: ${e.message}"
                    )
                )
            }
        }

        // Get asset metrics only
        get("/assets/metrics") {
            try {
                val filters = call.request.queryParameters.let { params ->
                    val locationId = params["locationId"]?.toIntOrNull()
                    val categoryId = params["categoryId"]?.toIntOrNull()

                    if (locationId != null || categoryId != null) {
                        DashboardFiltersDto(
                            locationId = locationId,
                            categoryId = categoryId
                        )
                    } else null
                }

                val metrics = dashboardService.getAssetMetrics(filters)
                call.respond(ApiResponse(
                    success = true,
                    data = metrics
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch asset metrics: ${e.message}"
                    )
                )
            }
        }

        // Get maintenance metrics only
        get("/maintenance/metrics") {
            try {
                val filters = call.request.queryParameters.let { params ->
                    val locationId = params["locationId"]?.toIntOrNull()
                    val categoryId = params["categoryId"]?.toIntOrNull()
                    val startDate = params["startDate"]
                    val endDate = params["endDate"]

                    if (locationId != null || categoryId != null || (startDate != null && endDate != null)) {
                        DashboardFiltersDto(
                            locationId = locationId,
                            categoryId = categoryId,
                            dateRange = if (startDate != null && endDate != null) {
                                DateRangeDto(startDate, endDate)
                            } else null
                        )
                    } else null
                }

                val metrics = dashboardService.getMaintenanceMetrics(filters)
                call.respond(ApiResponse(
                    success = true,
                    data = metrics
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch maintenance metrics: ${e.message}"
                    )
                )
            }
        }

        // Get software license metrics
        get("/licenses/metrics") {
            try {
                val metrics = dashboardService.getSoftwareLicenseMetrics()
                call.respond(ApiResponse(
                    success = true,
                    data = metrics
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch license metrics: ${e.message}"
                    )
                )
            }
        }

        // Get subscription metrics
        get("/subscriptions/metrics") {
            try {
                val metrics = dashboardService.getSubscriptionMetrics()
                call.respond(ApiResponse(
                    success = true,
                    data = metrics
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch subscription metrics: ${e.message}"
                    )
                )
            }
        }

        // Get issue metrics
        get("/issues/metrics") {
            try {
                val metrics = dashboardService.getIssueMetrics()
                call.respond(ApiResponse(
                    success = true,
                    data = metrics
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch issue metrics: ${e.message}"
                    )
                )
            }
        }

        // Get upcoming events
        get("/events/upcoming") {
            try {
                val events = dashboardService.getUpcomingEvents()
                call.respond(ApiResponse(
                    success = true,
                    data = events
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch upcoming events: ${e.message}"
                    )
                )
            }
        }

        // Get financial summary
        get("/financial/summary") {
            try {
                val summary = dashboardService.getFinancialSummary()
                call.respond(ApiResponse(
                    success = true,
                    data = summary
                ))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch financial summary: ${e.message}"
                    )
                )
            }
        }

        // Post endpoint for complex filtering (alternative to query params)
        post("/overview/filtered") {
            try {
                val filters = call.receive<DashboardFiltersDto>()
                val overview = dashboardService.getDashboardOverview(filters)
                call.respond(ApiResponse(
                    success = true,
                    data = overview
                ))
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Invalid filter format: ${e.message}"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse(
                        success = false,
                        data = null,
                        message = "Failed to fetch filtered dashboard overview: ${e.message}"
                    )
                )
            }
        }
    }
}