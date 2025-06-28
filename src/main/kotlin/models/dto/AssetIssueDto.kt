package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssetIssueDto(
    val id: Int? = null,
    val assetId: Int,
    val reportedBy: Int,
    val assignedTo: Int? = null,
    val issueType: String,
    val severity: String,
    val issueDescription: String,
    val resolutionNotes: String? = null,
    val status: String = "OPEN",
    val reportedAt: String? = null, // LocalDateTime as String
    val resolvedAt: String? = null, // LocalDateTime as String
    val closedAt: String? = null, // LocalDateTime as String
    // Additional fields for joined queries
    val assetName: String? = null,
    val assetSerialNumber: String? = null,
    val reportedByName: String? = null,
    val reportedByEmail: String? = null,
    val assignedToName: String? = null,
    val assignedToEmail: String? = null
)

@Serializable
data class CreateAssetIssueRequest(
    val assetId: Int,
    val reportedBy: Int,
    val assignedTo: Int? = null,
    val issueType: String,
    val severity: String,
    val issueDescription: String,
    val resolutionNotes: String? = null,
    val status: String = "OPEN"
)

@Serializable
data class UpdateAssetIssueRequest(
    val assetId: Int,
    val reportedBy: Int,
    val assignedTo: Int? = null,
    val issueType: String,
    val severity: String,
    val issueDescription: String,
    val resolutionNotes: String? = null,
    val status: String = "OPEN"
)

@Serializable
data class AssignIssueRequest(
    val assignedTo: Int
)

@Serializable
data class ResolveIssueRequest(
    val resolutionNotes: String,
    val status: String = "RESOLVED"
)

@Serializable
data class BulkCreateAssetIssueRequest(
    val assetIds: List<Int>,
    val reportedBy: Int,
    val assignedTo: Int? = null,
    val issueType: String,
    val severity: String,
    val issueDescription: String,
    val resolutionNotes: String? = null,
    val status: String = "OPEN"
)

@Serializable
data class AssetIssueHistoryDto(
    val assetId: Int,
    val assetName: String,
    val assetSerialNumber: String? = null,
    val totalIssues: Int,
    val openIssues: Int,
    val resolvedIssues: Int,
    val closedIssues: Int,
    val lastIssueDate: String? = null,
    val issues: List<AssetIssueDto>
)

@Serializable
data class IssueStatsDto(
    val totalIssues: Int,
    val issuesByStatus: Map<String, Int>,
    val issuesBySeverity: Map<String, Int>,
    val issuesByType: Map<String, Int>,
    val issuesByMonth: Map<String, Int>,
    val averageResolutionTimeHours: Double? = null,
    val openIssuesOlderThan30Days: Int
)

@Serializable
data class UserIssueStatsDto(
    val userId: Int,
    val userName: String,
    val reportedIssues: Int,
    val assignedIssues: Int,
    val resolvedIssues: Int,
    val pendingIssues: Int
)