package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceTypeDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val estimatedDurationHours: Int? = null,
    val costEstimate: String? = null // String representation of BigDecimal for JSON serialization
)

@Serializable
data class CreateMaintenanceTypeRequest(
    val name: String,
    val description: String? = null,
    val estimatedDurationHours: Int? = null,
    val costEstimate: String? = null // String representation of BigDecimal
)

@Serializable
data class UpdateMaintenanceTypeRequest(
    val name: String,
    val description: String? = null,
    val estimatedDurationHours: Int? = null,
    val costEstimate: String? = null // String representation of BigDecimal
)

// ===== REPOSITORY =====