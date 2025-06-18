package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateVendorRequest
import com.techbros.models.dto.UpdateVendorRequest
import com.techbros.models.dto.VendorDto
import com.techbros.models.dto.VendorSummaryDto
import com.techbros.repositories.VendorRepository
import io.ktor.http.*
import java.util.regex.Pattern

class VendorService(private val vendorRepository: VendorRepository) {

    // Email validation regex
    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    // Phone validation regex (basic pattern for various formats)
    private val phonePattern = Pattern.compile(
        "^[+]?[0-9\\s\\-\\(\\)]{7,20}$"
    )

    suspend fun createVendor(request: CreateVendorRequest): VendorDto {
        validateVendorRequest(request.name, request.contactEmail, request.contactPhone)

        val vendorId = vendorRepository.create(request)
        return getVendorById(vendorId)
    }

    suspend fun getVendorById(id: Int): VendorDto {
        return vendorRepository.findById(id)
            ?: throw ApiException("Vendor not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllVendors(): List<VendorDto> {
        return vendorRepository.findAll()
    }

    suspend fun getAllVendorsSummary(): List<VendorSummaryDto> {
        return vendorRepository.findAllSummary()
    }

    suspend fun updateVendor(id: Int, request: UpdateVendorRequest): VendorDto {
        validateVendorRequest(request.name, request.contactEmail, request.contactPhone)

        val updated = vendorRepository.update(id, request)
        if (!updated) {
            throw ApiException("Vendor not found", HttpStatusCode.NotFound)
        }

        return getVendorById(id)
    }

    suspend fun deleteVendor(id: Int) {
        // Note: You might want to add validation here to check if the vendor is being used
        // in any purchase orders, assets, or other related tables before allowing deletion.

        val deleted = vendorRepository.delete(id)
        if (!deleted) {
            throw ApiException("Vendor not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun searchVendors(searchTerm: String): List<VendorDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }

        return vendorRepository.searchByName(searchTerm)
    }

    suspend fun searchVendorsByContact(searchTerm: String): List<VendorDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }

        return vendorRepository.searchByContact(searchTerm)
    }

    suspend fun getVendorsByEmail(email: String): List<VendorDto> {
        if (!isValidEmail(email)) {
            throw ApiException("Invalid email format", HttpStatusCode.BadRequest)
        }

        return vendorRepository.findByEmail(email)
    }

    suspend fun getVendorsByPhone(phone: String): List<VendorDto> {
        if (!isValidPhone(phone)) {
            throw ApiException("Invalid phone format", HttpStatusCode.BadRequest)
        }

        return vendorRepository.findByPhone(phone)
    }

    suspend fun getVendorsWithEmail(): List<VendorDto> {
        return vendorRepository.findVendorsWithEmail()
    }

    suspend fun getVendorsWithPhone(): List<VendorDto> {
        return vendorRepository.findVendorsWithPhone()
    }

    suspend fun getVendorStats(): Map<String, Long> {
        val totalCount = vendorRepository.getCount()
        val withEmailCount = vendorRepository.getCountWithEmail()
        val withPhoneCount = vendorRepository.getCountWithPhone()

        return mapOf(
            "total" to totalCount,
            "withEmail" to withEmailCount,
            "withPhone" to withPhoneCount,
            "withoutEmail" to (totalCount - withEmailCount),
            "withoutPhone" to (totalCount - withPhoneCount)
        )
    }

    suspend fun validateVendorExists(id: Int) {
        if (!vendorRepository.exists(id)) {
            throw ApiException("Vendor not found", HttpStatusCode.NotFound)
        }
    }

    private fun validateVendorRequest(name: String, email: String?, phone: String?) {
        when {
            name.isBlank() -> throw ApiException("Vendor name cannot be empty", HttpStatusCode.BadRequest)
            name.length > 255 -> throw ApiException("Vendor name is too long (max 255 characters)", HttpStatusCode.BadRequest)
            name.trim() != name -> throw ApiException("Vendor name cannot have leading or trailing whitespace", HttpStatusCode.BadRequest)
        }

        // Validate email if provided
        if (!email.isNullOrBlank()) {
            when {
                email.length > 255 -> throw ApiException("Contact email is too long (max 255 characters)", HttpStatusCode.BadRequest)
                !isValidEmail(email) -> throw ApiException("Invalid email format", HttpStatusCode.BadRequest)
            }
        }

        // Validate phone if provided
        if (!phone.isNullOrBlank()) {
            when {
                phone.length > 20 -> throw ApiException("Contact phone is too long (max 20 characters)", HttpStatusCode.BadRequest)
                !isValidPhone(phone) -> throw ApiException("Invalid phone format", HttpStatusCode.BadRequest)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phonePattern.matcher(phone).matches()
    }
}