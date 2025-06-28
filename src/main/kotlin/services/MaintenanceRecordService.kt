package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.repositories.MaintenanceRecordRepository
import io.ktor.http.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.math.BigDecimal

class MaintenanceRecordService(
    private val maintenanceRecordRepository: MaintenanceRecordRepository,
    private val assetService: AssetService,
    private val userService: UserService,
    private val maintenanceScheduleService: MaintenanceScheduleService
) {

    suspend fun createMaintenanceRecord(request: CreateMaintenanceRecordRequest): MaintenanceRecordDto {
        validateMaintenanceRecordRequest(request)

        // Validate that referenced entities exist
        assetService.getAssetById(request.assetId)
        userService.getUserById(request.performedBy)

        if (request.scheduleId != null) {
            maintenanceScheduleService.getMaintenanceScheduleById(request.scheduleId)
        }

        val recordId = maintenanceRecordRepository.create(request)
        return getMaintenanceRecordById(recordId)
    }

    suspend fun bulkCreateMaintenanceRecords(request: BulkCreateMaintenanceRecordRequest): List<MaintenanceRecordDto> {
        validateId(request.performedBy, "Performed By")
        validateDate(request.performedDate, "Performed Date")
        validateMaintenanceType(request.maintenanceType)

        if (request.assetIds.isEmpty()) {
            throw ApiException("Asset IDs list cannot be empty", HttpStatusCode.BadRequest)
        }

        // Validate that referenced entities exist
        userService.getUserById(request.performedBy)

        val createRequests = mutableListOf<CreateMaintenanceRecordRequest>()

        for (assetId in request.assetIds) {
            validateId(assetId, "Asset ID")
            assetService.getAssetById(assetId)

            createRequests.add(CreateMaintenanceRecordRequest(
                assetId = assetId,
                scheduleId = null,
                performedBy = request.performedBy,
                maintenanceType = request.maintenanceType,
                performedDate = request.performedDate,
                durationHours = request.durationHours,
                cost = request.cost,
                description = request.description,
                partsReplaced = request.partsReplaced,
                nextMaintenanceDue = request.nextMaintenanceDue,
                status = request.status
            ))
        }

        val recordIds = maintenanceRecordRepository.bulkCreate(createRequests)
        return recordIds.map { getMaintenanceRecordById(it) }
    }

    suspend fun getMaintenanceRecordById(id: Int): MaintenanceRecordDto {
        validateId(id, "Maintenance Record ID")
        return maintenanceRecordRepository.findById(id)
            ?: throw ApiException("Maintenance record not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllMaintenanceRecords(): List<MaintenanceRecordDto> {
        return maintenanceRecordRepository.findAll()
    }

    suspend fun updateMaintenanceRecord(id: Int, request: UpdateMaintenanceRecordRequest): MaintenanceRecordDto {
        validateId(id, "Maintenance Record ID")
        validateMaintenanceRecordRequest(request)

        // Validate that referenced entities exist
        assetService.getAssetById(request.assetId)
        userService.getUserById(request.performedBy)

        if (request.scheduleId != null) {
            maintenanceScheduleService.getMaintenanceScheduleById(request.scheduleId)
        }

        val updated = maintenanceRecordRepository.update(id, request)
        if (!updated) {
            throw ApiException("Maintenance record not found", HttpStatusCode.NotFound)
        }

        return getMaintenanceRecordById(id)
    }

    suspend fun deleteMaintenanceRecord(id: Int) {
        validateId(id, "Maintenance Record ID")
        val deleted = maintenanceRecordRepository.delete(id)
        if (!deleted) {
            throw ApiException("Maintenance record not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getMaintenanceRecordsByAsset(assetId: Int): List<MaintenanceRecordDto> {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)
        return maintenanceRecordRepository.findByAssetId(assetId)
    }

    suspend fun getMaintenanceRecordsByPerformedBy(performedBy: Int): List<MaintenanceRecordDto> {
        validateId(performedBy, "Performed By")
        // Validate user exists
        userService.getUserById(performedBy)
        return maintenanceRecordRepository.findByPerformedBy(performedBy)
    }

    suspend fun getMaintenanceRecordsBySchedule(scheduleId: Int): List<MaintenanceRecordDto> {
        validateId(scheduleId, "Schedule ID")
        // Validate schedule exists
        maintenanceScheduleService.getMaintenanceScheduleById(scheduleId)
        return maintenanceRecordRepository.findByScheduleId(scheduleId)
    }

    suspend fun getMaintenanceRecordsByType(maintenanceType: String): List<MaintenanceRecordDto> {
        validateMaintenanceType(maintenanceType)
        return maintenanceRecordRepository.findByMaintenanceType(maintenanceType)
    }

    suspend fun getMaintenanceRecordsByStatus(status: String): List<MaintenanceRecordDto> {
        validateStatus(status)
        return maintenanceRecordRepository.findByStatus(status)
    }

    suspend fun getMaintenanceRecordsByDateRange(startDate: String, endDate: String): List<MaintenanceRecordDto> {
        validateDate(startDate, "Start Date")
        validateDate(endDate, "End Date")

        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        if (start.isAfter(end)) {
            throw ApiException("Start date cannot be after end date", HttpStatusCode.BadRequest)
        }

        return maintenanceRecordRepository.findByDateRange(startDate, endDate)
    }

    suspend fun getAssetMaintenanceHistory(assetId: Int): AssetMaintenanceHistoryDto {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)

        return maintenanceRecordRepository.getAssetMaintenanceHistory(assetId)
            ?: throw ApiException("No maintenance history found for this asset", HttpStatusCode.NotFound)
    }

    suspend fun getMaintenanceStats(): MaintenanceStatsDto {
        return maintenanceRecordRepository.getMaintenanceStats()
    }

    suspend fun deleteMaintenanceRecordsByAsset(assetId: Int): Int {
        validateId(assetId, "Asset ID")
        // Validate asset exists
        assetService.getAssetById(assetId)
        return maintenanceRecordRepository.deleteByAssetId(assetId)
    }

    suspend fun deleteMaintenanceRecordsBySchedule(scheduleId: Int): Int {
        validateId(scheduleId, "Schedule ID")
        // Validate schedule exists
        maintenanceScheduleService.getMaintenanceScheduleById(scheduleId)
        return maintenanceRecordRepository.deleteByScheduleId(scheduleId)
    }

    private fun validateMaintenanceRecordRequest(request: CreateMaintenanceRecordRequest) {
        validateId(request.assetId, "Asset ID")
        validateId(request.performedBy, "Performed By")
        validateDate(request.performedDate, "Performed Date")
        validateMaintenanceType(request.maintenanceType)
        validateStatus(request.status)

        if (request.scheduleId != null) {
            validateId(request.scheduleId, "Schedule ID")
        }

        if (request.durationHours != null) {
            validateDecimal(request.durationHours, "Duration Hours")
        }

        if (request.cost != null) {
            validateDecimal(request.cost, "Cost")
        }

        if (request.nextMaintenanceDue != null) {
            validateDate(request.nextMaintenanceDue, "Next Maintenance Due")
        }
    }

    private fun validateMaintenanceRecordRequest(request: UpdateMaintenanceRecordRequest) {
        validateId(request.assetId, "Asset ID")
        validateId(request.performedBy, "Performed By")
        validateDate(request.performedDate, "Performed Date")
        validateMaintenanceType(request.maintenanceType)
        validateStatus(request.status)

        if (request.scheduleId != null) {
            validateId(request.scheduleId, "Schedule ID")
        }

        if (request.durationHours != null) {
            validateDecimal(request.durationHours, "Duration Hours")
        }

        if (request.cost != null) {
            validateDecimal(request.cost, "Cost")
        }

        if (request.nextMaintenanceDue != null) {
            validateDate(request.nextMaintenanceDue, "Next Maintenance Due")
        }
    }

    private fun validateId(id: Int, fieldName: String) {
        if (id <= 0) {
            throw ApiException("$fieldName must be a positive integer", HttpStatusCode.BadRequest)
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
            BigDecimal(decimal)
        } catch (e: NumberFormatException) {
            throw ApiException("$fieldName must be a valid decimal number", HttpStatusCode.BadRequest)
        }
    }

    private fun validateMaintenanceType(maintenanceType: String) {
        if (maintenanceType.isBlank()) {
            throw ApiException("Maintenance type cannot be empty", HttpStatusCode.BadRequest)
        }
        if (maintenanceType.length > 100) {
            throw ApiException("Maintenance type cannot exceed 100 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateStatus(status: String) {
        val validStatuses = listOf("COMPLETED", "IN_PROGRESS", "CANCELLED", "SCHEDULED")
        if (status !in validStatuses) {
            throw ApiException("Status must be one of: ${validStatuses.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }
}