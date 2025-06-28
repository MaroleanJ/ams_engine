package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateSubscriptionRequest
import com.techbros.models.dto.UpdateSubscriptionRequest
import com.techbros.models.dto.BulkCreateSubscriptionRequest
import com.techbros.models.responses.ApiResponse
import com.techbros.services.SubscriptionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.subscriptionRoutes(subscriptionService: SubscriptionService) {
    route("/api/v1/subscriptions") {

        // Get all subscriptions
        get {
            val subscriptions = subscriptionService.getAllSubscriptions()
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Create a new subscription
        post {
            val request = call.receive<CreateSubscriptionRequest>()
            val subscription = subscriptionService.createSubscription(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = subscription,
                message = "Subscription created successfully"
            ))
        }

        // Bulk create subscriptions
        post("/bulk") {
            val request = call.receive<BulkCreateSubscriptionRequest>()
            val subscriptions = subscriptionService.bulkCreateSubscriptions(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = subscriptions,
                message = "${subscriptions.size} subscriptions created successfully"
            ))
        }

        // Get subscription statistics
        get("/stats") {
            val stats = subscriptionService.getSubscriptionStats()
            call.respond(ApiResponse(
                success = true,
                data = stats
            ))
        }

        // Get subscriptions expiring this month
        get("/expiring-this-month") {
            val expiring = subscriptionService.getExpiringSubscriptionsThisMonth()
            call.respond(ApiResponse(
                success = true,
                data = expiring
            ))
        }

        // Get subscriptions by auto renewal status
        get("/auto-renewal") {
            val autoRenewal = subscriptionService.getAutoRenewalSubscriptions()
            call.respond(ApiResponse(
                success = true,
                data = autoRenewal
            ))
        }

        // Get subscriptions by manual renewal status
        get("/manual-renewal") {
            val manualRenewal = subscriptionService.getManualRenewalSubscriptions()
            call.respond(ApiResponse(
                success = true,
                data = manualRenewal
            ))
        }

        // Get subscriptions by vendor
        get("/vendor/{vendorId}") {
            val vendorId = call.parameters["vendorId"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val subscriptions = subscriptionService.getSubscriptionsByVendor(vendorId)
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Delete subscriptions by vendor
        delete("/vendor/{vendorId}") {
            val vendorId = call.parameters["vendorId"]?.toIntOrNull()
                ?: throw ApiException("Invalid vendor ID", HttpStatusCode.BadRequest)

            val deletedCount = subscriptionService.deleteSubscriptionsByVendor(vendorId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "$deletedCount subscriptions deleted for vendor"
            ))
        }

        // Get subscriptions by assigned user
        get("/assigned-to/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val subscriptions = subscriptionService.getSubscriptionsByAssignedTo(userId)
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Delete subscriptions by assigned user
        delete("/assigned-to/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val deletedCount = subscriptionService.deleteSubscriptionsByAssignedTo(userId)
            call.respond(ApiResponse(
                success = true,
                data = mapOf("deletedCount" to deletedCount),
                message = "$deletedCount subscriptions deleted for user"
            ))
        }

        // Get subscriptions by status
        get("/status/{status}") {
            val status = call.parameters["status"]
                ?: throw ApiException("Status parameter is required", HttpStatusCode.BadRequest)

            val subscriptions = subscriptionService.getSubscriptionsByStatus(status)
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Get subscriptions by billing cycle
        get("/billing-cycle/{billingCycle}") {
            val billingCycle = call.parameters["billingCycle"]
                ?: throw ApiException("Billing cycle parameter is required", HttpStatusCode.BadRequest)

            val subscriptions = subscriptionService.getSubscriptionsByBillingCycle(billingCycle)
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Get subscriptions expiring between dates
        get("/expiring") {
            val startDate = call.request.queryParameters["startDate"]
                ?: throw ApiException("Start date parameter is required", HttpStatusCode.BadRequest)
            val endDate = call.request.queryParameters["endDate"]
                ?: throw ApiException("End date parameter is required", HttpStatusCode.BadRequest)

            val subscriptions = subscriptionService.getExpiringSubscriptions(startDate, endDate)
            call.respond(ApiResponse(
                success = true,
                data = subscriptions
            ))
        }

        // Get subscription by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid subscription ID", HttpStatusCode.BadRequest)

            val subscription = subscriptionService.getSubscriptionById(id)
            call.respond(ApiResponse(
                success = true,
                data = subscription
            ))
        }

        // Update subscription
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid subscription ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateSubscriptionRequest>()
            val subscription = subscriptionService.updateSubscription(id, request)
            call.respond(ApiResponse(
                success = true,
                data = subscription,
                message = "Subscription updated successfully"
            ))
        }

        // Delete subscription
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid subscription ID", HttpStatusCode.BadRequest)

            subscriptionService.deleteSubscription(id)
            call.respond(ApiResponse(
                success = true,
                data = null,
                message = "Subscription deleted successfully"
            ))
        }
    }
}