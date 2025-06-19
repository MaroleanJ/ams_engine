package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.AssetDetailDto
import com.techbros.models.dto.AssetDto
import com.techbros.models.dto.AssetSummaryDto
import com.techbros.models.dto.CreateAssetRequest
import com.techbros.models.dto.UpdateAssetRequest
import com.techbros.repositories.AssetRepository
import io.ktor.http.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

class AssetService(
    private val assetRepository: AssetRepository,
    private val categoryService: AssetCategoryService,
    private val vendorService: VendorService,
    private val locationService: LocationService,
    private val userService: UserService
) {

    // Barcode validation regex (basic alphanumeric pattern)
    private val barcodePattern = Pattern.compile("^[A-Za-z0-9\\-_]+$")

    // Serial number validation regex (basic alphanumeric pattern)
    private val serialNumberPattern = Pattern.compile("^[A-Za-z0-9\\-_]+$")

    suspend fun createAsset(request: CreateAssetRequest): AssetDto {
        print("Asset creation request")
        validateAssetRequest(request)
        validateReferences(request.categoryId, request.vendorId, request.locationId, request.assignedTo)
        validateUniqueFields(request.serialNumber, request.barcode)

        val assetId = assetRepository.create(request)
        return getAssetById(assetId)
    }

    suspend fun getAssetById(id: Int): AssetDto {
        return assetRepository.findById(id)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)
    }

    suspend fun getAssetDetailById(id: Int): AssetDetailDto {
        return assetRepository.findDetailById(id)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllAssets(): List<AssetDto> {
        return assetRepository.findAll()
    }

    suspend fun getAllAssetsSummary(): List<AssetSummaryDto> {
        return assetRepository.findAllSummary()
    }

    suspend fun getAssetsByCategory(categoryId: Int): List<AssetDto> {
        // Validate that category exists
        categoryService.validateCategoryExists(categoryId)
        return assetRepository.findByCategory(categoryId)
    }

    suspend fun getAssetsByVendor(vendorId: Int): List<AssetDto> {
        // Validate that vendor exists
        vendorService.validateVendorExists(vendorId)
        return assetRepository.findByVendor(vendorId)
    }

    suspend fun getAssetsByLocation(locationId: Int): List<AssetDto> {
        // Validate that location exists
        locationService.validateLocationExists(locationId)
        return assetRepository.findByLocation(locationId)
    }

    suspend fun getAssetsByAssignedUser(userId: Int): List<AssetDto> {
        // Validate that user exists
        userService.validateUserExists(userId)
        return assetRepository.findByAssignedUser(userId)
    }

    suspend fun getAssetsByStatus(status: String): List<AssetDto> {
        if (status.isBlank()) {
            throw ApiException("Status cannot be empty", HttpStatusCode.BadRequest)
        }
        return assetRepository.findByStatus(status)
    }

    suspend fun getAssetBySerialNumber(serialNumber: String): AssetDto {
        if (serialNumber.isBlank()) {
            throw ApiException("Serial number cannot be empty", HttpStatusCode.BadRequest)
        }
        return assetRepository.findBySerialNumber(serialNumber)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)
    }

    suspend fun getAssetByBarcode(barcode: String): AssetDto {
        if (barcode.isBlank()) {
            throw ApiException("Barcode cannot be empty", HttpStatusCode.BadRequest)
        }
        return assetRepository.findByBarcode(barcode)
            ?: throw ApiException("Asset not found", HttpStatusCode.NotFound)
    }

    suspend fun searchAssets(searchTerm: String): List<AssetDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }
        return assetRepository.searchByName(searchTerm)
    }

    suspend fun searchAssetsByNameOrModel(searchTerm: String): List<AssetDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }
        return assetRepository.searchByNameOrModel(searchTerm)
    }

    suspend fun getAssetsWithWarrantyExpiring(days: Long = 30): List<AssetDto> {
        if (days < 0) {
            throw ApiException("Days must be non-negative", HttpStatusCode.BadRequest)
        }
        return assetRepository.findAssetsWithWarrantyExpiring(days)
    }

    suspend fun getUnassignedAssets(): List<AssetDto> {
        return assetRepository.findUnassignedAssets()
    }

    suspend fun updateAsset(id: Int, request: UpdateAssetRequest): AssetDto {
        validateAssetRequest(request)
        validateReferences(request.categoryId, request.vendorId, request.locationId, request.assignedTo)
        validateUniqueFields(request.serialNumber, request.barcode, excludeId = id)

        val updated = assetRepository.update(id, request)
        if (!updated) {
            throw ApiException("Asset not found", HttpStatusCode.NotFound)
        }

        return getAssetById(id)
    }

    suspend fun deleteAsset(id: Int) {
        val deleted = assetRepository.delete(id)
        if (!deleted) {
            throw ApiException("Asset not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getAssetStats(): Map<String, Any> {
        val totalCount = assetRepository.getCount()
        val countByStatus = assetRepository.getCountByStatus()
        val countByCategory = assetRepository.getCountByCategory()
        val totalValue = assetRepository.getTotalValue()

        return mapOf(
            "total" to totalCount,
            "countByStatus" to countByStatus,
            "countByCategory" to countByCategory,
            "totalValue" to totalValue.toString(),
            "unassigned" to assetRepository.findUnassignedAssets().size
        )
    }

    suspend fun validateAssetExists(id: Int) {
        if (!assetRepository.exists(id)) {
            throw ApiException("Asset not found", HttpStatusCode.NotFound)
        }
    }

    private suspend fun validateAssetRequest(request: CreateAssetRequest) {
        validateAssetRequest(
            request.name,
            request.serialNumber,
            request.barcode,
            request.purchaseDate,
            request.purchasePrice,
            request.currentValue,
            request.depreciationRate,
            request.warrantyExpiry,
            request.status
        )
    }

    private suspend fun validateAssetRequest(request: UpdateAssetRequest) {
        validateAssetRequest(
            request.name,
            request.serialNumber,
            request.barcode,
            request.purchaseDate,
            request.purchasePrice,
            request.currentValue,
            request.depreciationRate,
            request.warrantyExpiry,
            request.status
        )
    }

    private fun validateAssetRequest(
        name: String,
        serialNumber: String?,
        barcode: String?,
        purchaseDate: String?,
        purchasePrice: String?,
        currentValue: String?,
        depreciationRate: String?,
        warrantyExpiry: String?,
        status: String?
    ) {
        // Validate name
        when {
            name.isBlank() -> throw ApiException("Asset name cannot be empty", HttpStatusCode.BadRequest)
            name.length > 255 -> throw ApiException("Asset name is too long (max 255 characters)", HttpStatusCode.BadRequest)
            name.trim() != name -> throw ApiException("Asset name cannot have leading or trailing whitespace", HttpStatusCode.BadRequest)
        }

        // Validate serial number if provided
        if (!serialNumber.isNullOrBlank()) {
            when {
                serialNumber.length > 100 -> throw ApiException("Serial number is too long (max 100 characters)", HttpStatusCode.BadRequest)
                !isValidSerialNumber(serialNumber) -> throw ApiException("Invalid serial number format", HttpStatusCode.BadRequest)
            }
        }

        // Validate barcode if provided
        if (!barcode.isNullOrBlank()) {
            when {
                barcode.length > 100 -> throw ApiException("Barcode is too long (max 100 characters)", HttpStatusCode.BadRequest)
                !isValidBarcode(barcode) -> throw ApiException("Invalid barcode format", HttpStatusCode.BadRequest)
            }
        }

        // Validate dates
        purchaseDate?.let { validateDate(it, "Purchase date") }
        warrantyExpiry?.let { validateDate(it, "Warranty expiry date") }

        // Validate monetary values
        purchasePrice?.let { validateDecimal(it, "Purchase price", 10, 2) }
        currentValue?.let { validateDecimal(it, "Current value", 10, 2) }
        depreciationRate?.let { validateDecimal(it, "Depreciation rate", 5, 2) }

        // Validate status if provided
        if (!status.isNullOrBlank()) {
            when {
                status.length > 50 -> throw ApiException("Status is too long (max 50 characters)", HttpStatusCode.BadRequest)
            }
        }
    }

    private suspend fun validateReferences(
        categoryId: Int,
        vendorId: Int?,
        locationId: Int?,
        assignedTo: Int?
    ) {
        // Category is required
        categoryService.validateCategoryExists(categoryId)

        // Other references are optional
        vendorId?.let { vendorService.validateVendorExists(it) }
        locationId?.let { locationService.validateLocationExists(it) }
        assignedTo?.let { userService.validateUserExists(it) }
    }

    private suspend fun validateUniqueFields(
        serialNumber: String?,
        barcode: String?,
        excludeId: Int? = null
    ) {
        serialNumber?.let {
            if (assetRepository.serialNumberExists(it, excludeId)) {
                throw ApiException("Serial number already exists", HttpStatusCode.Conflict)
            }
        }

        barcode?.let {
            if (assetRepository.barcodeExists(it, excludeId)) {
                throw ApiException("Barcode already exists", HttpStatusCode.Conflict)
            }
        }
    }

    private fun validateDate(dateString: String, fieldName: String) {
        try {
            LocalDate.parse(dateString)
        } catch (e: DateTimeParseException) {
            throw ApiException("Invalid $fieldName format. Use ISO date format (YYYY-MM-DD)", HttpStatusCode.BadRequest)
        }
    }

    private fun validateDecimal(decimalString: String, fieldName: String, precision: Int, scale: Int) {
        try {
            val decimal = BigDecimal(decimalString)

            // Check if the decimal has more digits than allowed
            val totalDigits = decimal.precision()
            val decimalPlaces = decimal.scale()

            if (totalDigits > precision) {
                throw ApiException("$fieldName has too many total digits (max $precision)", HttpStatusCode.BadRequest)
            }

            if (decimalPlaces > scale) {
                throw ApiException("$fieldName has too many decimal places (max $scale)", HttpStatusCode.BadRequest)
            }

        } catch (e: NumberFormatException) {
            throw ApiException("Invalid $fieldName format", HttpStatusCode.BadRequest)
        }
    }

    private fun isValidSerialNumber(serialNumber: String): Boolean {
        return serialNumberPattern.matcher(serialNumber).matches()
    }

    private fun isValidBarcode(barcode: String): Boolean {
        return barcodePattern.matcher(barcode).matches()
    }
}