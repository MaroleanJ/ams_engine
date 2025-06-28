package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.repositories.AssetIssueRepository
import io.ktor.http.*
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class AssetIssueService(
    private val assetIssueRepository: AssetIssueRepository,
    private val assetService: AssetService,
    private val userService: UserService
) {

    suspend fun createAssetIssue(request: CreateAssetIssueRequest): AssetIssueDto {
        validateAssetIssueRequest(request)

        // Validate that referenced entities exist
        assetService.getAssetById(request.assetId)
        userService.getUserById(request.reportedBy)

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val issueId = assetIssueRepository.create(request)
        return getAssetIssueById(issueId)
    }

    suspend fun bulkCreateAssetIssues(request: BulkCreateAssetIssueRequest): List<AssetIssueDto> {
        validateId(request.reportedBy, "Reported By")
        validateIssueType(request.issueType)
        validateSeverity(request.severity)
        validateStatus(request.status)

        if (request.assetIds.isEmpty()) {
            throw ApiException("Asset IDs list cannot be empty", HttpStatusCode.BadRequest)
        }

        // Validate that referenced entities exist
        userService.getUserById(request.reportedBy)

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val createRequests = mutableListOf<CreateAssetIssueRequest>()

        for (assetId in request.assetIds) {
            validateId(assetId, "Asset ID")
            assetService.getAssetById(assetId)

            createRequests.add(CreateAssetIssueRequest(
                assetId = assetId,
                reportedBy = request.reportedBy,
                assignedTo = request.assignedTo,
                issueType = request.issueType,
                severity = request.severity,
                issueDescription = request.issueDescription,
                resolutionNotes = request.resolutionNotes,
                status = request.status
            ))
        }

        val issueIds = assetIssueRepository.bulkCreate(createRequests)
        return issueIds.map { getAssetIssueById(it) }
    }

    suspend fun getAssetIssueById(id: Int): AssetIssueDto {
        validateId(id, "Asset Issue ID")
        return assetIssueRepository.findById(id)
            ?: throw ApiException("Asset issue not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllAssetIssues(): List<AssetIssueDto> {
        return assetIssueRepository.findAll()
    }

    suspend fun updateAssetIssue(id: Int, request: UpdateAssetIssueRequest): AssetIssueDto {
        validateId(id, "Asset Issue ID")
        validateAssetIssueRequest(request)

        // Validate that referenced entities exist
        assetService.getAssetById(request.assetId)
        userService.getUserById(request.reportedBy)

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val updated = assetIssueRepository.update(id, request)
        if (!updated) {
            throw ApiException("Asset issue not found", HttpStatusCode.NotFound)
        }

        return getAssetIssueById(id)
    }

    suspend fun deleteAssetIssue(id: Int) {
        validateId(id, "Asset Issue ID")
        val deleted = assetIssueRepository.delete(id)
        if (!deleted) {
            throw ApiException("Asset issue not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getAssetIssuesByAsset(assetId: Int): List<AssetIssueDto> {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)
        return assetIssueRepository.findByAssetId(assetId)
    }

    suspend fun getAssetIssuesByReportedBy(reportedBy: Int): List<AssetIssueDto> {
        validateId(reportedBy, "Reported By")
        // Validate user exists
        userService.getUserById(reportedBy)
        return assetIssueRepository.findByReportedBy(reportedBy)
    }

    suspend fun getAssetIssuesByAssignedTo(assignedTo: Int): List<AssetIssueDto> {
        validateId(assignedTo, "Assigned To")
        // Validate user exists
        userService.getUserById(assignedTo)
        return assetIssueRepository.findByAssignedTo(assignedTo)
    }

    suspend fun getAssetIssuesByType(issueType: String): List<AssetIssueDto> {
        validateIssueType(issueType)
        return assetIssueRepository.findByIssueType(issueType)
    }

    suspend fun getAssetIssuesBySeverity(severity: String): List<AssetIssueDto> {
        validateSeverity(severity)
        return assetIssueRepository.findBySeverity(severity)
    }

    suspend fun getAssetIssuesByStatus(status: String): List<AssetIssueDto> {
        validateStatus(status)
        return assetIssueRepository.findByStatus(status)
    }

    suspend fun getAssetIssuesByDateRange(startDate: String, endDate: String): List<AssetIssueDto> {
        validateDateTime(startDate, "Start Date")
        validateDateTime(endDate, "End Date")

        val start = LocalDateTime.parse(startDate)
        val end = LocalDateTime.parse(endDate)

        if (start.isAfter(end)) {
            throw ApiException("Start date cannot be after end date", HttpStatusCode.BadRequest)
        }

        return assetIssueRepository.findByDateRange(start, end)
    }

    suspend fun assignIssue(id: Int, request: AssignIssueRequest): AssetIssueDto {
        validateId(id, "Asset Issue ID")
        validateId(request.assignedTo, "Assigned To")

        // Validate user exists
        userService.getUserById(request.assignedTo)

        val updated = assetIssueRepository.assignIssue(id, request.assignedTo)
        if (!updated) {
            throw ApiException("Asset issue not found", HttpStatusCode.NotFound)
        }

        return getAssetIssueById(id)
    }

    suspend fun resolveIssue(id: Int, request: ResolveIssueRequest): AssetIssueDto {
        validateId(id, "Asset Issue ID")
        validateStatus(request.status)

        if (request.resolutionNotes.isBlank()) {
            throw ApiException("Resolution notes cannot be empty", HttpStatusCode.BadRequest)
        }

        val updated = assetIssueRepository.resolveIssue(id, request.resolutionNotes, request.status)
        if (!updated) {
            throw ApiException("Asset issue not found", HttpStatusCode.NotFound)
        }

        return getAssetIssueById(id)
    }

    suspend fun getAssetIssueHistory(assetId: Int): AssetIssueHistoryDto {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)

        return assetIssueRepository.getAssetIssueHistory(assetId)
            ?: throw ApiException("No issue history found for this asset", HttpStatusCode.NotFound)
    }

    suspend fun getIssueStats(): IssueStatsDto {
        return assetIssueRepository.getIssueStats()
    }

    suspend fun getUserIssueStats(userId: Int): UserIssueStatsDto {
        validateId(userId, "User ID")
        // Validate user exists
        userService.getUserById(userId)

        return assetIssueRepository.getUserIssueStats(userId)
            ?: throw ApiException("No issue statistics found for this user", HttpStatusCode.NotFound)
    }

    suspend fun deleteAssetIssuesByAsset(assetId: Int): Int {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)
        return assetIssueRepository.deleteByAssetId(assetId)
    }

    // Validation helper methods
    private fun validateAssetIssueRequest(request: CreateAssetIssueRequest) {
        validateId(request.assetId, "Asset ID")
        validateId(request.reportedBy, "Reported By")
        validateIssueType(request.issueType)
        validateSeverity(request.severity)
        validateStatus(request.status)

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.issueDescription.isBlank()) {
            throw ApiException("Issue description cannot be empty", HttpStatusCode.BadRequest)
        }

        if (request.issueDescription.length > 5000) {
            throw ApiException("Issue description cannot exceed 5000 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateAssetIssueRequest(request: UpdateAssetIssueRequest) {
        validateId(request.assetId, "Asset ID")
        validateId(request.reportedBy, "Reported By")
        validateIssueType(request.issueType)
        validateSeverity(request.severity)
        validateStatus(request.status)

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.issueDescription.isBlank()) {
            throw ApiException("Issue description cannot be empty", HttpStatusCode.BadRequest)
        }

        if (request.issueDescription.length > 5000) {
            throw ApiException("Issue description cannot exceed 5000 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateId(id: Int, fieldName: String) {
        if (id <= 0) {
            throw ApiException("$fieldName must be a positive integer", HttpStatusCode.BadRequest)
        }
    }

    private fun validateDateTime(dateTime: String, fieldName: String) {
        try {
            LocalDateTime.parse(dateTime)
        } catch (e: DateTimeParseException) {
            throw ApiException("$fieldName must be a valid date-time (ISO format)", HttpStatusCode.BadRequest)
        }
    }

    private fun validateIssueType(issueType: String) {
        if (issueType.isBlank()) {
            throw ApiException("Issue type cannot be empty", HttpStatusCode.BadRequest)
        }
        if (issueType.length > 50) {
            throw ApiException("Issue type cannot exceed 50 characters", HttpStatusCode.BadRequest)
        }

        val validIssueTypes = listOf(
            "HARDWARE_FAILURE", "SOFTWARE_ISSUE", "NETWORK_PROBLEM",
            "PERFORMANCE_ISSUE", "SECURITY_INCIDENT", "MAINTENANCE_REQUIRED",
            "USER_ERROR", "CONFIGURATION_ISSUE", "OTHER"
        )

        if (issueType !in validIssueTypes) {
            throw ApiException("Issue type must be one of: ${validIssueTypes.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }

    private fun validateSeverity(severity: String) {
        if (severity.isBlank()) {
            throw ApiException("Severity cannot be empty", HttpStatusCode.BadRequest)
        }

        val validSeverities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        if (severity !in validSeverities) {
            throw ApiException("Severity must be one of: ${validSeverities.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }

    private fun validateStatus(status: String) {
        if (status.isBlank()) {
            throw ApiException("Status cannot be empty", HttpStatusCode.BadRequest)
        }

        val validStatuses = listOf("OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED", "CANCELLED")
        if (status !in validStatuses) {
            throw ApiException("Status must be one of: ${validStatuses.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }
}