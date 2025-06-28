package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    val id: Int? = null,
    val name: String,
    val vendorId: Int? = null,
    val plan: String? = null,
    val startDate: String? = null, // LocalDate as String
    val expiryDate: String? = null, // LocalDate as String
    val renewalDate: String? = null, // LocalDate as String
    val cost: String? = null, // Decimal as String
    val billingCycle: String? = null,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE",
    val autoRenewal: Boolean = false,
    val createdAt: String? = null, // LocalDateTime as String
    // Additional fields for joined queries
    val vendorName: String? = null,
    val vendorEmail: String? = null,
    val assignedToName: String? = null,
    val assignedToEmail: String? = null
)

@Serializable
data class CreateSubscriptionRequest(
    val name: String,
    val vendorId: Int? = null,
    val plan: String? = null,
    val startDate: String? = null,
    val expiryDate: String? = null,
    val renewalDate: String? = null,
    val cost: String? = null,
    val billingCycle: String? = null,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE",
    val autoRenewal: Boolean = false
)

@Serializable
data class UpdateSubscriptionRequest(
    val name: String,
    val vendorId: Int? = null,
    val plan: String? = null,
    val startDate: String? = null,
    val expiryDate: String? = null,
    val renewalDate: String? = null,
    val cost: String? = null,
    val billingCycle: String? = null,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE",
    val autoRenewal: Boolean = false
)

@Serializable
data class BulkCreateSubscriptionRequest(
    val subscriptions: List<CreateSubscriptionRequest>
)

@Serializable
data class SubscriptionStatsDto(
    val totalSubscriptions: Int,
    val totalCost: String? = null,
    val averageCost: String? = null,
    val subscriptionsByStatus: Map<String, Int>,
    val subscriptionsByBillingCycle: Map<String, Int>,
    val expiringThisMonth: Int,
    val autoRenewalCount: Int
)

@Serializable
data class SubscriptionRenewalDto(
    val id: Int,
    val name: String,
    val expiryDate: String,
    val renewalDate: String? = null,
    val cost: String? = null,
    val billingCycle: String? = null,
    val autoRenewal: Boolean,
    val daysUntilExpiry: Long,
    val assignedToName: String? = null,
    val vendorName: String? = null
)