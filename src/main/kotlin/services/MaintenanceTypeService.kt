package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateMaintenanceTypeRequest
import com.techbros.models.dto.MaintenanceTypeDto
import com.techbros.models.dto.UpdateMaintenanceTypeRequest
import com.techbros.repositories.MaintenanceTypeRepository
import io.ktor.http.*
import java.math.BigDecimal
import java.math.RoundingMode

class MaintenanceTypeService(private val maintenanceTypeRepository: MaintenanceTypeRepository) {

    suspend fun createMaintenanceType(request: CreateMaintenanceTypeRequest): MaintenanceTypeDto {
        validateMaintenanceTypeRequest(request.name, request.estimatedDurationHours, request.costEstimate)

        // Check if name already exists
        if (maintenanceTypeRepository.existsByName(request.name)) {
            throw ApiException("Maintenance type with this name already exists", HttpStatusCode.Conflict)
        }

        val maintenanceTypeId = maintenanceTypeRepository.create(request)
        return getMaintenanceTypeById(maintenanceTypeId)
    }

    suspend fun getMaintenanceTypeById(id: Int): MaintenanceTypeDto {
        return maintenanceTypeRepository.findById(id)
            ?: throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllMaintenanceTypes(): List<MaintenanceTypeDto> {
        return maintenanceTypeRepository.findAll()
    }

    suspend fun getMaintenanceTypeByName(name: String): MaintenanceTypeDto {
        return maintenanceTypeRepository.findByName(name)
            ?: throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
    }

    suspend fun searchMaintenanceTypesByName(namePattern: String): List<MaintenanceTypeDto> {
        if (namePattern.isBlank()) {
            throw ApiException("Search pattern cannot be empty", HttpStatusCode.BadRequest)
        }
        return maintenanceTypeRepository.findByNameContaining(namePattern)
    }

    suspend fun getMaintenanceTypesByDurationRange(minHours: Int?, maxHours: Int?): List<MaintenanceTypeDto> {
        if (minHours != null && minHours < 0) {
            throw ApiException("Minimum hours cannot be negative", HttpStatusCode.BadRequest)
        }
        if (maxHours != null && maxHours < 0) {
            throw ApiException("Maximum hours cannot be negative", HttpStatusCode.BadRequest)
        }
        if (minHours != null && maxHours != null && minHours > maxHours) {
            throw ApiException("Minimum hours cannot be greater than maximum hours", HttpStatusCode.BadRequest)
        }

        return maintenanceTypeRepository.findByDurationRange(minHours, maxHours)
    }

    suspend fun getMaintenanceTypesByCostRange(minCost: String?, maxCost: String?): List<MaintenanceTypeDto> {
        val minCostDecimal = minCost?.let { validateAndParseCost(it, "Minimum cost") }
        val maxCostDecimal = maxCost?.let { validateAndParseCost(it, "Maximum cost") }

        if (minCostDecimal != null && maxCostDecimal != null && minCostDecimal > maxCostDecimal) {
            throw ApiException("Minimum cost cannot be greater than maximum cost", HttpStatusCode.BadRequest)
        }

        return maintenanceTypeRepository.findByCostRange(minCostDecimal, maxCostDecimal)
    }

    suspend fun updateMaintenanceType(id: Int, request: UpdateMaintenanceTypeRequest): MaintenanceTypeDto {
        validateMaintenanceTypeRequest(request.name, request.estimatedDurationHours, request.costEstimate)

        // Check if name already exists (excluding current record)
        if (maintenanceTypeRepository.existsByName(request.name, excludeId = id)) {
            throw ApiException("Maintenance type with this name already exists", HttpStatusCode.Conflict)
        }

        val updated = maintenanceTypeRepository.update(id, request)
        if (!updated) {
            throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
        }

        return getMaintenanceTypeById(id)
    }

    suspend fun deleteMaintenanceType(id: Int) {
        val deleted = maintenanceTypeRepository.delete(id)
        if (!deleted) {
            throw ApiException("Maintenance type not found", HttpStatusCode.NotFound)
        }
    }

    private fun validateMaintenanceTypeRequest(
        name: String, 
        estimatedDurationHours: Int?, 
        costEstimate: String?
    ) {
        when {
            name.isBlank() -> throw ApiException("Maintenance type name cannot be empty", HttpStatusCode.BadRequest)
            name.length > 100 -> throw ApiException("Maintenance type name is too long (max 100 characters)", HttpStatusCode.BadRequest)
        }

        estimatedDurationHours?.let { hours ->
            if (hours < 0) {
                throw ApiException("Estimated duration hours cannot be negative", HttpStatusCode.BadRequest)
            }
            if (hours > 8760) { // More than a year in hours
                throw ApiException("Estimated duration hours is unrealistic (max 8760 hours)", HttpStatusCode.BadRequest)
            }
        }

        costEstimate?.let { cost ->
            validateAndParseCost(cost, "Cost estimate")
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
            decimal.setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            throw ApiException("$fieldName must be a valid decimal number", HttpStatusCode.BadRequest)
        }
    }
}

// ===== ROUTES =====