package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceRecordDto(
    val id: Int? = null,
    val assetId: Int,
    val scheduleId: Int? = null,
    val performedBy: Int,
    val maintenanceType: String,
    val performedDate: String, // LocalDate as String
    val durationHours: String? = null, // Decimal as String
    val cost: String? = null, // Decimal as String
    val description: String? = null,
    val partsReplaced: String? = null,
    val nextMaintenanceDue: String? = null, // LocalDate as String
    val status: String = "COMPLETED",
    val createdAt: String? = null, // LocalDateTime as String
    // Additional fields for joined queries
    val assetName: String? = null,
    val assetSerialNumber: String? = null,
    val performedByName: String? = null,
    val performedByEmail: String? = null,
    val scheduleName: String? = null
)

@Serializable
data class CreateMaintenanceRecordRequest(
    val assetId: Int,
    val scheduleId: Int? = null,
    val performedBy: Int,
    val maintenanceType: String,
    val performedDate: String, // LocalDate as String
    val durationHours: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val partsReplaced: String? = null,
    val nextMaintenanceDue: String? = null,
    val status: String = "COMPLETED"
)

@Serializable
data class UpdateMaintenanceRecordRequest(
    val assetId: Int,
    val scheduleId: Int? = null,
    val performedBy: Int,
    val maintenanceType: String,
    val performedDate: String,
    val durationHours: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val partsReplaced: String? = null,
    val nextMaintenanceDue: String? = null,
    val status: String = "COMPLETED"
)

@Serializable
data class BulkCreateMaintenanceRecordRequest(
    val assetIds: List<Int>,
    val performedBy: Int,
    val maintenanceType: String,
    val performedDate: String,
    val durationHours: String? = null,
    val cost: String? = null,
    val description: String? = null,
    val partsReplaced: String? = null,
    val nextMaintenanceDue: String? = null,
    val status: String = "COMPLETED"
)

@Serializable
data class AssetMaintenanceHistoryDto(
    val assetId: Int,
    val assetName: String,
    val assetSerialNumber: String? = null,
    val totalMaintenanceRecords: Int,
    val totalCost: String? = null,
    val lastMaintenanceDate: String? = null,
    val records: List<MaintenanceRecordDto>
)

@Serializable
data class MaintenanceStatsDto(
    val totalRecords: Int,
    val totalCost: String? = null,
    val averageCost: String? = null,
    val recordsByStatus: Map<String, Int>,
    val recordsByType: Map<String, Int>,
    val recordsByMonth: Map<String, Int>
)