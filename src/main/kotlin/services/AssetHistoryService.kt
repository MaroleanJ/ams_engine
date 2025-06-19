package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.AssetHistoryDto
import com.techbros.models.dto.CreateAssetHistoryRequest
import com.techbros.models.dto.UpdateAssetHistoryRequest
import com.techbros.repositories.AssetHistoryRepository
import com.techbros.repositories.AssetRepository
import com.techbros.repositories.UserRepository
import io.ktor.http.*
import java.time.Instant
import java.time.format.DateTimeParseException

class AssetHistoryService(
    private val assetHistoryRepository: AssetHistoryRepository,
    private val assetRepository: AssetRepository,
    private val userRepository: UserRepository
) {

    suspend fun createAssetHistory(request: CreateAssetHistoryRequest): AssetHistoryDto {
        validateAssetHistoryRequest(request)

        // Verify asset and user exist
        assetRepository.findById(request.assetId)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)

        userRepository.findById(request.changedBy)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)

        val historyId = assetHistoryRepository.create(request)
        return getAssetHistoryById(historyId)
    }

    suspend fun getAssetHistoryById(id: Int): AssetHistoryDto {
        return assetHistoryRepository.findById(id)
            ?: throw ApiException("Asset history record not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllAssetHistory(): List<AssetHistoryDto> {
        return assetHistoryRepository.findAll()
    }

    suspend fun getAssetHistoryByAssetId(assetId: Int): List<AssetHistoryDto> {
        // Verify asset exists
        assetRepository.findById(assetId)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)

        return assetHistoryRepository.findByAssetId(assetId)
    }

    suspend fun getAssetHistoryByUser(userId: Int): List<AssetHistoryDto> {
        // Verify user exists
        userRepository.findById(userId)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)

        return assetHistoryRepository.findByUser(userId)
    }

    suspend fun getAssetHistoryByActionType(actionType: String): List<AssetHistoryDto> {
        validateActionType(actionType)
        return assetHistoryRepository.findByActionType(actionType)
    }

    suspend fun getAssetHistoryByDateRange(startDate: String, endDate: String): List<AssetHistoryDto> {
        val start = validateAndParseTimestamp(startDate, "Start date")
        val end = validateAndParseTimestamp(endDate, "End date")

        if (start.isAfter(end)) {
            throw ApiException("Start date must be before end date", HttpStatusCode.BadRequest)
        }

        return assetHistoryRepository.findByDateRange(start, end)
    }

    suspend fun updateAssetHistory(id: Int, request: UpdateAssetHistoryRequest): AssetHistoryDto {
        validateUpdateRequest(request)

        val updated = assetHistoryRepository.update(id, request)
        if (!updated) {
            throw ApiException("Asset history record not found", HttpStatusCode.NotFound)
        }

        return getAssetHistoryById(id)
    }

    suspend fun deleteAssetHistory(id: Int) {
        val deleted = assetHistoryRepository.delete(id)
        if (!deleted) {
            throw ApiException("Asset history record not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun deleteAssetHistoryByAssetId(assetId: Int): Int {
        // Verify asset exists
        assetRepository.findById(assetId)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)

        return assetHistoryRepository.deleteByAssetId(assetId)
    }

    private fun validateAssetHistoryRequest(request: CreateAssetHistoryRequest) {
        when {
            request.assetId <= 0 -> throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)
            request.changedBy <= 0 -> throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)
            request.actionType.isBlank() -> throw ApiException("Action type cannot be empty", HttpStatusCode.BadRequest)
            request.actionType.length > 50 -> throw ApiException("Action type is too long (max 50 characters)", HttpStatusCode.BadRequest)
            request.fieldChanged?.length ?: 0 > 100 -> throw ApiException("Field changed is too long (max 100 characters)", HttpStatusCode.BadRequest)
        }

        validateActionType(request.actionType)
    }

    private fun validateUpdateRequest(request: UpdateAssetHistoryRequest) {
        when {
            request.actionType.isBlank() -> throw ApiException("Action type cannot be empty", HttpStatusCode.BadRequest)
            request.actionType.length > 50 -> throw ApiException("Action type is too long (max 50 characters)", HttpStatusCode.BadRequest)
            request.fieldChanged?.length ?: 0 > 100 -> throw ApiException("Field changed is too long (max 100 characters)", HttpStatusCode.BadRequest)
        }

        validateActionType(request.actionType)
    }

    private fun validateActionType(actionType: String) {
        val validActionTypes = setOf(
            "CREATED", "UPDATED", "DELETED", "ASSIGNED", "UNASSIGNED",
            "MOVED", "MAINTAINED", "REPAIRED", "DISPOSED", "ARCHIVED"
        )

        if (actionType.uppercase() !in validActionTypes) {
            throw ApiException(
                "Invalid action type. Valid types: ${validActionTypes.joinToString(", ")}",
                HttpStatusCode.BadRequest
            )
        }
    }

    private fun validateAndParseTimestamp(timestamp: String, fieldName: String): Instant {
        return try {
            Instant.parse(timestamp)
        } catch (e: DateTimeParseException) {
            throw ApiException("$fieldName must be in ISO format (e.g., 2023-12-01T10:00:00Z)", HttpStatusCode.BadRequest)
        }
    }
}