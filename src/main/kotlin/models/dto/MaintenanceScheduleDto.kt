package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceScheduleDto(
    val id: Int? = null,
    val assetId: Int,
    val assetName: String? = null, // For display purposes
    val maintenanceTypeId: Int? = null,
    val maintenanceType: String? = null,
    val maintenanceTypeName: String? = null, // From maintenance_types table
    val frequencyDays: Int,
    val lastPerformed: String? = null, // Format: YYYY-MM-DD
    val nextDue: String, // Format: YYYY-MM-DD
    val assignedTo: Int? = null,
    val assignedToName: String? = null, // For display purposes
    val priority: String? = null,
    val estimatedCost: String? = null, // String representation of BigDecimal
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String, // ISO format timestamp
    val isOverdue: Boolean = false, // Calculated field
    val daysUntilDue: Int? = null // Calculated field
)

@Serializable
data class CreateMaintenanceScheduleRequest(
    val assetId: Int,
    val maintenanceTypeId: Int? = null,
    val maintenanceType: String? = null,
    val frequencyDays: Int,
    val lastPerformed: String? = null, // Format: YYYY-MM-DD
    val nextDue: String, // Format: YYYY-MM-DD
    val assignedTo: Int? = null,
    val priority: String? = null,
    val estimatedCost: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UpdateMaintenanceScheduleRequest(
    val maintenanceTypeId: Int? = null,
    val maintenanceType: String? = null,
    val frequencyDays: Int,
    val lastPerformed: String? = null, // Format: YYYY-MM-DD
    val nextDue: String, // Format: YYYY-MM-DD
    val assignedTo: Int? = null,
    val priority: String? = null,
    val estimatedCost: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class MaintenanceScheduleSummaryDto(
    val totalSchedules: Int,
    val activeSchedules: Int,
    val overdueSchedules: Int,
    val dueTodaySchedules: Int,
    val dueThisWeekSchedules: Int,
    val totalEstimatedCost: String? = null
)
