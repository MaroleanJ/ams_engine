package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.repositories.SubscriptionRepository
import io.ktor.http.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.math.BigDecimal

class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val vendorService: VendorService,
    private val userService: UserService
) {

    suspend fun createSubscription(request: CreateSubscriptionRequest): SubscriptionDto {
        validateSubscriptionRequest(request)

        // Validate that referenced entities exist
        if (request.vendorId != null) {
            vendorService.getVendorById(request.vendorId)
        }

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val subscriptionId = subscriptionRepository.create(request)
        return getSubscriptionById(subscriptionId)
    }

    suspend fun bulkCreateSubscriptions(request: BulkCreateSubscriptionRequest): List<SubscriptionDto> {
        if (request.subscriptions.isEmpty()) {
            throw ApiException("Subscriptions list cannot be empty", HttpStatusCode.BadRequest)
        }

        // Validate all requests
        request.subscriptions.forEach { validateSubscriptionRequest(it) }

        // Validate that referenced entities exist
        val vendorIds = request.subscriptions.mapNotNull { it.vendorId }.distinct()
        val assignedToIds = request.subscriptions.mapNotNull { it.assignedTo }.distinct()

        vendorIds.forEach { vendorService.getVendorById(it) }
        assignedToIds.forEach { userService.getUserById(it) }

        val subscriptionIds = subscriptionRepository.bulkCreate(request.subscriptions)
        return subscriptionIds.map { getSubscriptionById(it) }
    }

    suspend fun getSubscriptionById(id: Int): SubscriptionDto {
        validateId(id, "Subscription ID")
        return subscriptionRepository.findById(id)
            ?: throw ApiException("Subscription not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllSubscriptions(): List<SubscriptionDto> {
        return subscriptionRepository.findAll()
    }

    suspend fun updateSubscription(id: Int, request: UpdateSubscriptionRequest): SubscriptionDto {
        validateId(id, "Subscription ID")
        validateSubscriptionRequest(request)

        // Validate that referenced entities exist
        if (request.vendorId != null) {
            vendorService.getVendorById(request.vendorId)
        }

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val updated = subscriptionRepository.update(id, request)
        if (!updated) {
            throw ApiException("Subscription not found", HttpStatusCode.NotFound)
        }

        return getSubscriptionById(id)
    }

    suspend fun deleteSubscription(id: Int) {
        validateId(id, "Subscription ID")
        val deleted = subscriptionRepository.delete(id)
        if (!deleted) {
            throw ApiException("Subscription not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getSubscriptionsByVendor(vendorId: Int): List<SubscriptionDto> {
        validateId(vendorId, "Vendor ID")
        // Validate vendor exists
        vendorService.getVendorById(vendorId)
        return subscriptionRepository.findByVendorId(vendorId)
    }

    suspend fun getSubscriptionsByAssignedTo(assignedTo: Int): List<SubscriptionDto> {
        validateId(assignedTo, "Assigned To")
        // Validate user exists
        userService.getUserById(assignedTo)
        return subscriptionRepository.findByAssignedTo(assignedTo)
    }

    suspend fun getSubscriptionsByStatus(status: String): List<SubscriptionDto> {
        validateStatus(status)
        return subscriptionRepository.findByStatus(status)
    }

    suspend fun getSubscriptionsByBillingCycle(billingCycle: String): List<SubscriptionDto> {
        validateBillingCycle(billingCycle)
        return subscriptionRepository.findByBillingCycle(billingCycle)
    }

    suspend fun getExpiringSubscriptions(startDate: String, endDate: String): List<SubscriptionDto> {
        validateDate(startDate, "Start Date")
        validateDate(endDate, "End Date")

        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        if (start.isAfter(end)) {
            throw ApiException("Start date cannot be after end date", HttpStatusCode.BadRequest)
        }

        return subscriptionRepository.findExpiringBetween(startDate, endDate)
    }

    suspend fun getExpiringSubscriptionsThisMonth(): List<SubscriptionRenewalDto> {
        return subscriptionRepository.findExpiringThisMonth()
    }

    suspend fun getAutoRenewalSubscriptions(): List<SubscriptionDto> {
        return subscriptionRepository.findByAutoRenewal(true)
    }

    suspend fun getManualRenewalSubscriptions(): List<SubscriptionDto> {
        return subscriptionRepository.findByAutoRenewal(false)
    }

    suspend fun getSubscriptionStats(): SubscriptionStatsDto {
        return subscriptionRepository.getSubscriptionStats()
    }

    suspend fun deleteSubscriptionsByVendor(vendorId: Int): Int {
        validateId(vendorId, "Vendor ID")
        // Validate vendor exists
        vendorService.getVendorById(vendorId)
        return subscriptionRepository.deleteByVendorId(vendorId)
    }

    suspend fun deleteSubscriptionsByAssignedTo(assignedTo: Int): Int {
        validateId(assignedTo, "Assigned To")
        // Validate user exists
        userService.getUserById(assignedTo)
        return subscriptionRepository.deleteByAssignedTo(assignedTo)
    }

    private fun validateSubscriptionRequest(request: CreateSubscriptionRequest) {
        validateName(request.name)
        validateStatus(request.status)

        if (request.vendorId != null) {
            validateId(request.vendorId, "Vendor ID")
        }

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.startDate != null) {
            validateDate(request.startDate, "Start Date")
        }

        if (request.expiryDate != null) {
            validateDate(request.expiryDate, "Expiry Date")
        }

        if (request.renewalDate != null) {
            validateDate(request.renewalDate, "Renewal Date")
        }

        if (request.cost != null) {
            validateDecimal(request.cost, "Cost")
        }

        if (request.billingCycle != null) {
            validateBillingCycle(request.billingCycle)
        }

        // Validate date logic
        if (request.startDate != null && request.expiryDate != null) {
            val startDate = LocalDate.parse(request.startDate)
            val expiryDate = LocalDate.parse(request.expiryDate)
            if (startDate.isAfter(expiryDate)) {
                throw ApiException("Start date cannot be after expiry date", HttpStatusCode.BadRequest)
            }
        }
    }

    private fun validateSubscriptionRequest(request: UpdateSubscriptionRequest) {
        validateName(request.name)
        validateStatus(request.status)

        if (request.vendorId != null) {
            validateId(request.vendorId, "Vendor ID")
        }

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.startDate != null) {
            validateDate(request.startDate, "Start Date")
        }

        if (request.expiryDate != null) {
            validateDate(request.expiryDate, "Expiry Date")
        }

        if (request.renewalDate != null) {
            validateDate(request.renewalDate, "Renewal Date")
        }

        if (request.cost != null) {
            validateDecimal(request.cost, "Cost")
        }

        if (request.billingCycle != null) {
            validateBillingCycle(request.billingCycle)
        }

        // Validate date logic
        if (request.startDate != null && request.expiryDate != null) {
            val startDate = LocalDate.parse(request.startDate)
            val expiryDate = LocalDate.parse(request.expiryDate)
            if (startDate.isAfter(expiryDate)) {
                throw ApiException("Start date cannot be after expiry date", HttpStatusCode.BadRequest)
            }
        }
    }

    private fun validateId(id: Int, fieldName: String) {
        if (id <= 0) {
            throw ApiException("$fieldName must be a positive integer", HttpStatusCode.BadRequest)
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ApiException("Subscription name cannot be empty", HttpStatusCode.BadRequest)
        }
        if (name.length > 255) {
            throw ApiException("Subscription name cannot exceed 255 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateDate(date: String, fieldName: String) {
        try {
            LocalDate.parse(date)
        } catch (e: DateTimeParseException) {
            throw ApiException("$fieldName must be a valid date (YYYY-MM-DD)", HttpStatusCode.BadRequest)
        }
    }

    private fun validateDecimal(decimal: String, fieldName: String) {
        try {
            val value = BigDecimal(decimal)
            if (value < BigDecimal.ZERO) {
                throw ApiException("$fieldName cannot be negative", HttpStatusCode.BadRequest)
            }
        } catch (e: NumberFormatException) {
            throw ApiException("$fieldName must be a valid decimal number", HttpStatusCode.BadRequest)
        }
    }

    private fun validateStatus(status: String) {
        val validStatuses = listOf("ACTIVE", "INACTIVE", "EXPIRED", "CANCELLED", "SUSPENDED")
        if (status !in validStatuses) {
            throw ApiException("Status must be one of: ${validStatuses.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }

    private fun validateBillingCycle(billingCycle: String) {
        val validCycles = listOf("MONTHLY", "QUARTERLY", "SEMI_ANNUALLY", "ANNUALLY", "ONE_TIME")
        if (billingCycle !in validCycles) {
            throw ApiException("Billing cycle must be one of: ${validCycles.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }
}