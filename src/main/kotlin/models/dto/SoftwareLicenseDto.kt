package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class SoftwareLicenseDto(
    val id: Int? = null,
    val name: String,
    val vendorId: Int? = null,
    val licenseKey: String? = null,
    val startDate: String? = null, // LocalDate as String
    val expiryDate: String? = null, // LocalDate as String
    val numberOfSeats: Int? = null,
    val seatsUsed: Int = 0,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE",
    val createdAt: String? = null, // LocalDateTime as String
    // Additional fields for joined queries
    val vendorName: String? = null,
    val vendorContactEmail: String? = null,
    val assignedToName: String? = null,
    val assignedToEmail: String? = null,
    val availableSeats: Int? = null, // Calculated field: numberOfSeats - seatsUsed
    val isExpired: Boolean? = null, // Calculated field based on expiry date
    val daysUntilExpiry: Long? = null // Calculated field
)

@Serializable
data class CreateSoftwareLicenseRequest(
    val name: String,
    val vendorId: Int? = null,
    val licenseKey: String? = null,
    val startDate: String? = null,
    val expiryDate: String? = null,
    val numberOfSeats: Int? = null,
    val seatsUsed: Int = 0,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class UpdateSoftwareLicenseRequest(
    val name: String,
    val vendorId: Int? = null,
    val licenseKey: String? = null,
    val startDate: String? = null,
    val expiryDate: String? = null,
    val numberOfSeats: Int? = null,
    val seatsUsed: Int = 0,
    val assignedTo: Int? = null,
    val notes: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class SoftwareLicenseStatsDto(
    val totalLicenses: Int,
    val activeLicenses: Int,
    val expiredLicenses: Int,
    val expiringIn30Days: Int,
    val totalSeats: Int,
    val usedSeats: Int,
    val availableSeats: Int,
    val licensesByStatus: Map<String, Int>,
    val licensesByVendor: Map<String, Int>,
    val upcomingExpirations: List<SoftwareLicenseDto>
)

@Serializable
data class SeatUsageRequest(
    val seatsToAdd: Int
)