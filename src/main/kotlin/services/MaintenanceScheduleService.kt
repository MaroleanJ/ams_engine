package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateMaintenanceScheduleRequest
import com.techbros.models.dto.MaintenanceScheduleDto
import com.techbros.models.dto.MaintenanceScheduleSummaryDto
import com.techbros.models.dto.UpdateMaintenanceScheduleRequest
import com.techbros.repositories.AssetRepository
import com.techbros.repositories.MaintenanceScheduleRepository
import com.techbros.repositories.MaintenanceTypeRepository
import com.techbros.repositories.UserRepository
import io.ktor.http.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

class MaintenanceScheduleService(
    private val maintenanceScheduleRepository: MaintenanceScheduleRepository,
    private val assetRepository: AssetRepository,
    private val maintenanceTypeRepository: MaintenanceTypeRepository,
    private val userRepository: UserRepository
) {

    suspend fun createMaintenanceSchedule(request: CreateMaintenanceScheduleRequest): MaintenanceScheduleDto {
        validateMaintenanceScheduleRequest(request)

        // Verify asset exists
        assetRepository.findById(request.assetId)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)

        // Verify maintenance type exists if provided
        request.maintenanceTypeId?.let { typeId ->
            maintenanceTypeRepository.findById(typeId)
                ?: throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
        }

        // Verify assigned user exists if provided
        request.assignedTo?.let { userId ->
            userRepository.findById(userId)
                ?: throw ApiException("Assigned user not found", HttpStatusCode.NotFound)
        }

        val scheduleId = maintenanceScheduleRepository.create(request)
        return getMaintenanceScheduleById(scheduleId)
    }

    suspend fun getMaintenanceScheduleById(id: Int): MaintenanceScheduleDto {
        return maintenanceScheduleRepository.findById(id)
            ?: throw ApiException("Maintenance schedule not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllMaintenanceSchedules(): List<MaintenanceScheduleDto> {
        return maintenanceScheduleRepository.findAll()
    }

    suspend fun getMaintenanceSchedulesByAssetId(assetId: Int): List<MaintenanceScheduleDto> {
        // Verify asset exists
        assetRepository.findById(assetId)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)

        return maintenanceScheduleRepository.findByAssetId(assetId)
    }

    suspend fun getMaintenanceSchedulesByAssignedUser(userId: Int): List<MaintenanceScheduleDto> {
        // Verify user exists
        userRepository.findById(userId)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)

        return maintenanceScheduleRepository.findByAssignedUser(userId)
    }

    suspend fun getMaintenanceSchedulesByPriority(priority: String): List<MaintenanceScheduleDto> {
        validatePriority(priority)
        return maintenanceScheduleRepository.findByPriority(priority)
    }

    suspend fun getActiveMaintenanceSchedules(): List<MaintenanceScheduleDto> {
        return maintenanceScheduleRepository.findActiveSchedules()
    }

    suspend fun getOverdueMaintenanceSchedules(): List<MaintenanceScheduleDto> {
        return maintenanceScheduleRepository.findOverdueSchedules()
    }

    suspend fun getMaintenanceSchedulesDueInRange(startDate: String, endDate: String): List<MaintenanceScheduleDto> {
        val start = validateAndParseDate(startDate, "Start date")
        val end = validateAndParseDate(endDate, "End date")

        if (start.isAfter(end)) {
            throw ApiException("Start date must be before end date", HttpStatusCode.BadRequest)
        }

        return maintenanceScheduleRepository.findDueInRange(start, end)
    }

    suspend fun getMaintenanceSchedulesDueToday(): List<MaintenanceScheduleDto> {
        return maintenanceScheduleRepository.findDueToday()
    }

    suspend fun getMaintenanceSchedulesDueThisWeek(): List<MaintenanceScheduleDto> {
        return maintenanceScheduleRepository.findDueThisWeek()
    }

    suspend fun getMaintenanceScheduleSummary(): MaintenanceScheduleSummaryDto {
        return maintenanceScheduleRepository.getScheduleSummary()
    }

    suspend fun updateMaintenanceSchedule(id: Int, request: UpdateMaintenanceScheduleRequest): MaintenanceScheduleDto {
        validateUpdateRequest(request)

        // Verify maintenance type exists if provided
        request.maintenanceTypeId?.let { typeId ->
            maintenanceTypeRepository.findById(typeId)
                ?: throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
        }

        // Verify assigned user exists if provided
        request.assignedTo?.let { userId ->
            userRepository.findById(userId)
                ?: throw ApiException("Assigned user not found", HttpStatusCode.NotFound)
        }

        val updated = maintenanceScheduleRepository.update(id, request)
        if (!updated) {
            throw ApiException("Maintenance schedule not found", HttpStatusCode.NotFound)
        }

        return getMaintenanceScheduleById(id)
    }

    suspend fun completeMaintenanceSchedule(id: Int, completedDate: String): MaintenanceScheduleDto {
        val schedule = getMaintenanceScheduleById(id)
        val completed = validateAndParseDate(completedDate, "Completed date")
        val nextDue = completed.plusDays(schedule.frequencyDays.toLong())

        val updated = maintenanceScheduleRepository.updateNextDueDate(id, nextDue, completed)
        if (!updated) {
            throw ApiException("Failed to update maintenance schedule", HttpStatusCode.InternalServerError)
        }

        return getMaintenanceScheduleById(id)
    }

    suspend fun deactivateMaintenanceSchedule(id: Int): MaintenanceScheduleDto {
        val updated = maintenanceScheduleRepository.deactivateSchedule(id)
        if (!updated) {
            throw ApiException("Maintenance schedule not found", HttpStatusCode.NotFound)
        }

        return getMaintenanceScheduleById(id)
    }

    suspend fun deleteMaintenanceSchedule(id: Int) {
        val deleted = maintenanceScheduleRepository.delete(id)
        if (!deleted) {
            throw ApiException("Maintenance schedule not found", HttpStatusCode.NotFound)
        }
    }

    private fun validateMaintenanceScheduleRequest(request: CreateMaintenanceScheduleRequest) {
        when {
            request.assetId <= 0 -> throw ApiException("Invalid asset ID", HttpStatusCode.BadRequest)
            request.frequencyDays <= 0 -> throw ApiException("Frequency days must be positive", HttpStatusCode.BadRequest)
            request.frequencyDays > 3650 -> throw ApiException("Frequency days cannot exceed 10 years", HttpStatusCode.BadRequest)
        }

        validateAndParseDate(request.nextDue, "Next due date")
        
        request.lastPerformed?.let { date ->
            validateAndParseDate(date, "Last performed date")
        }

        request.priority?.let { priority ->
            validatePriority(priority)
        }

        request.estimatedCost?.let { cost ->
            validateAndParseCost(cost, "Estimated cost")
        }

        // Validate maintenance type consistency
        if (request.maintenanceTypeId != null && request.maintenanceType.isNullOrBlank()) {
            throw ApiException("Maintenance type name is required when maintenance type ID is provided", HttpStatusCode.BadRequest)
        }
    }

    private fun validateUpdateRequest(request: UpdateMaintenanceScheduleRequest) {
        when {
            request.frequencyDays <= 0 -> throw ApiException("Frequency days must be positive", HttpStatusCode.BadRequest)
            request.frequencyDays > 3650 -> throw ApiException("Frequency days cannot exceed 10 years", HttpStatusCode.BadRequest)
        }

        validateAndParseDate(request.nextDue, "Next due date")
        
        request.lastPerformed?.let { date ->
            validateAndParseDate(date, "Last performed date")
        }

        request.priority?.let { priority ->
            validatePriority(priority)
        }

        request.estimatedCost?.let { cost ->
            validateAndParseCost(cost, "Estimated cost")
        }
    }

    private fun validatePriority(priority: String) {
        val validPriorities = setOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        if (priority.uppercase() !in validPriorities) {
            throw ApiException(
                "Invalid priority. Valid priorities: ${validPriorities.joinToString(", ")}", 
                HttpStatusCode.BadRequest
            )
        }
    }

    private fun validateAndParseDate(date: String, fieldName: String): LocalDate {
        return try {
            LocalDate.parse(date)
        } catch (e: DateTimeParseException) {
            throw ApiException("$fieldName must be in format YYYY-MM-DD", HttpStatusCode.BadRequest)
        }
    }

    private fun validateAndParseCost(cost: String, fieldName: String): BigDecimal {
        return try {
            val decimal = BigDecimal(cost)
            if (decimal < BigDecimal.ZERO) {
                throw ApiException("$fieldName cannot be negative", HttpStatusCode.BadRequest)
            }
            if (decimal.scale() > 2) {
                throw ApiException("$fieldName cannot have more than 2 decimal places", HttpStatusCode.BadRequest)
            }
            if (decimal > BigDecimal("99999999.99")) {
                throw ApiException("$fieldName is too large (max 99999999.99)", HttpStatusCode.BadRequest)
            }
            decimal
        } catch (e: NumberFormatException) {
            throw ApiException("$fieldName must be a valid decimal number", HttpStatusCode.BadRequest)
        }
    }
}

// ===== ROUTES COMPLETION =====