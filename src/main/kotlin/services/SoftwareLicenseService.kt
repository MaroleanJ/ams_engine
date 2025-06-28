package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.*
import com.techbros.repositories.SoftwareLicenseRepository
import io.ktor.http.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

class SoftwareLicenseService(
    private val softwareLicenseRepository: SoftwareLicenseRepository,
    private val vendorService: VendorService,
    private val userService: UserService
) {

    suspend fun createSoftwareLicense(request: CreateSoftwareLicenseRequest): SoftwareLicenseDto {
        validateSoftwareLicenseRequest(request)

        // Validate that referenced entities exist
        if (request.vendorId != null) {
            vendorService.getVendorById(request.vendorId)
        }

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val licenseId = softwareLicenseRepository.create(request)
        return getSoftwareLicenseById(licenseId)
    }

    suspend fun getSoftwareLicenseById(id: Int): SoftwareLicenseDto {
        validateId(id, "Software License ID")
        return softwareLicenseRepository.findById(id)
            ?: throw ApiException("Software license not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllSoftwareLicenses(): List<SoftwareLicenseDto> {
        return softwareLicenseRepository.findAll()
    }

    suspend fun updateSoftwareLicense(id: Int, request: UpdateSoftwareLicenseRequest): SoftwareLicenseDto {
        validateId(id, "Software License ID")
        validateSoftwareLicenseRequest(request)

        // Validate that referenced entities exist
        if (request.vendorId != null) {
            vendorService.getVendorById(request.vendorId)
        }

        if (request.assignedTo != null) {
            userService.getUserById(request.assignedTo)
        }

        val updated = softwareLicenseRepository.update(id, request)
        if (!updated) {
            throw ApiException("Software license not found", HttpStatusCode.NotFound)
        }

        return getSoftwareLicenseById(id)
    }

    suspend fun deleteSoftwareLicense(id: Int) {
        validateId(id, "Software License ID")
        val deleted = softwareLicenseRepository.delete(id)
        if (!deleted) {
            throw ApiException("Software license not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getSoftwareLicensesByVendor(vendorId: Int): List<SoftwareLicenseDto> {
        validateId(vendorId, "Vendor ID")
        // Validate vendor exists
        vendorService.getVendorById(vendorId)
        return softwareLicenseRepository.findByVendorId(vendorId)
    }

    suspend fun getSoftwareLicensesByAssignedTo(assignedTo: Int): List<SoftwareLicenseDto> {
        validateId(assignedTo, "Assigned To")
        // Validate user exists
        userService.getUserById(assignedTo)
        return softwareLicenseRepository.findByAssignedTo(assignedTo)
    }

    suspend fun getSoftwareLicensesByStatus(status: String): List<SoftwareLicenseDto> {
        validateStatus(status)
        return softwareLicenseRepository.findByStatus(status)
    }

    suspend fun getExpiredSoftwareLicenses(): List<SoftwareLicenseDto> {
        return softwareLicenseRepository.findExpiredLicenses()
    }

    suspend fun getSoftwareLicensesExpiringInDays(days: Int): List<SoftwareLicenseDto> {
        if (days < 0) {
            throw ApiException("Days must be a non-negative integer", HttpStatusCode.BadRequest)
        }
        return softwareLicenseRepository.findExpiringInDays(days)
    }

    suspend fun getSoftwareLicensesByDateRange(startDate: String, endDate: String): List<SoftwareLicenseDto> {
        validateDate(startDate, "Start Date")
        validateDate(endDate, "End Date")

        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)

        if (start.isAfter(end)) {
            throw ApiException("Start date cannot be after end date", HttpStatusCode.BadRequest)
        }

        return softwareLicenseRepository.findByDateRange(startDate, endDate)
    }

    suspend fun updateSeatUsage(id: Int, request: SeatUsageRequest): SoftwareLicenseDto {
        validateId(id, "Software License ID")

        val license = getSoftwareLicenseById(id)
        val currentSeatsUsed = license.seatsUsed
        val newSeatsUsed = currentSeatsUsed + request.seatsToAdd

        if (newSeatsUsed < 0) {
            throw ApiException("Cannot have negative seats used", HttpStatusCode.BadRequest)
        }

        if (license.numberOfSeats != null && newSeatsUsed > license.numberOfSeats) {
            throw ApiException("Cannot exceed total number of seats", HttpStatusCode.BadRequest)
        }

        val updated = softwareLicenseRepository.updateSeatUsage(id, newSeatsUsed)
        if (!updated) {
            throw ApiException("Software license not found", HttpStatusCode.NotFound)
        }

        return getSoftwareLicenseById(id)
    }

    suspend fun getSoftwareLicenseStats(): SoftwareLicenseStatsDto {
        return softwareLicenseRepository.getSoftwareLicenseStats()
    }

    suspend fun deleteSoftwareLicensesByVendor(vendorId: Int): Int {
        validateId(vendorId, "Vendor ID")
        // Validate vendor exists
        vendorService.getVendorById(vendorId)
        return softwareLicenseRepository.deleteByVendorId(vendorId)
    }

    private fun validateSoftwareLicenseRequest(request: CreateSoftwareLicenseRequest) {
        validateName(request.name)
        validateStatus(request.status)

        if (request.vendorId != null) {
            validateId(request.vendorId, "Vendor ID")
        }

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.startDate != null) {
            validateDate(request.startDate, "Start Date")
        }

        if (request.expiryDate != null) {
            validateDate(request.expiryDate, "Expiry Date")
        }

        if (request.startDate != null && request.expiryDate != null) {
            val start = LocalDate.parse(request.startDate)
            val end = LocalDate.parse(request.expiryDate)
            if (start.isAfter(end)) {
                throw ApiException("Start date cannot be after expiry date", HttpStatusCode.BadRequest)
            }
        }

        if (request.numberOfSeats != null && request.numberOfSeats <= 0) {
            throw ApiException("Number of seats must be positive", HttpStatusCode.BadRequest)
        }

        if (request.seatsUsed < 0) {
            throw ApiException("Seats used cannot be negative", HttpStatusCode.BadRequest)
        }

        if (request.numberOfSeats != null && request.seatsUsed > request.numberOfSeats) {
            throw ApiException("Seats used cannot exceed total number of seats", HttpStatusCode.BadRequest)
        }
    }

    private fun validateSoftwareLicenseRequest(request: UpdateSoftwareLicenseRequest) {
        validateName(request.name)
        validateStatus(request.status)

        if (request.vendorId != null) {
            validateId(request.vendorId, "Vendor ID")
        }

        if (request.assignedTo != null) {
            validateId(request.assignedTo, "Assigned To")
        }

        if (request.startDate != null) {
            validateDate(request.startDate, "Start Date")
        }

        if (request.expiryDate != null) {
            validateDate(request.expiryDate, "Expiry Date")
        }

        if (request.startDate != null && request.expiryDate != null) {
            val start = LocalDate.parse(request.startDate)
            val end = LocalDate.parse(request.expiryDate)
            if (start.isAfter(end)) {
                throw ApiException("Start date cannot be after expiry date", HttpStatusCode.BadRequest)
            }
        }

        if (request.numberOfSeats != null && request.numberOfSeats <= 0) {
            throw ApiException("Number of seats must be positive", HttpStatusCode.BadRequest)
        }

        if (request.seatsUsed < 0) {
            throw ApiException("Seats used cannot be negative", HttpStatusCode.BadRequest)
        }

        if (request.numberOfSeats != null && request.seatsUsed > request.numberOfSeats) {
            throw ApiException("Seats used cannot exceed total number of seats", HttpStatusCode.BadRequest)
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

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ApiException("Name cannot be empty", HttpStatusCode.BadRequest)
        }
        if (name.length > 255) {
            throw ApiException("Name cannot exceed 255 characters", HttpStatusCode.BadRequest)
        }
    }

    private fun validateStatus(status: String) {
        val validStatuses = listOf("ACTIVE", "INACTIVE", "EXPIRED", "CANCELLED")
        if (status !in validStatuses) {
            throw ApiException("Status must be one of: ${validStatuses.joinToString(", ")}", HttpStatusCode.BadRequest)
        }
    }
}