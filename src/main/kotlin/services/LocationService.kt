package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateLocationRequest
import com.techbros.models.dto.LocationDto
import com.techbros.models.dto.LocationHierarchyDto
import com.techbros.models.dto.UpdateLocationRequest
import com.techbros.repositories.LocationRepository
import io.ktor.http.*

class LocationService(private val locationRepository: LocationRepository) {

    suspend fun createLocation(request: CreateLocationRequest): LocationDto {
        validateLocationRequest(request.name, request.parentLocationId)

        val locationId = locationRepository.create(request)
        return getLocationById(locationId)
    }

    suspend fun getLocationById(id: Int): LocationDto {
        return locationRepository.findById(id)
            ?: throw ApiException("Location not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllLocations(): List<LocationDto> {
        return locationRepository.findAll()
    }

    suspend fun getLocationsByParent(parentId: Int?): List<LocationDto> {
        // Validate parent exists if provided
        if (parentId != null) {
            getLocationById(parentId) // This will throw if not found
        }

        return locationRepository.findByParentId(parentId)
    }

    suspend fun getRootLocations(): List<LocationDto> {
        return locationRepository.findRootLocations()
    }

    suspend fun updateLocation(id: Int, request: UpdateLocationRequest): LocationDto {
        validateLocationRequest(request.name, request.parentLocationId)

        // Prevent circular references
        if (request.parentLocationId != null) {
            validateNoCircularReference(id, request.parentLocationId)
        }

        val updated = locationRepository.update(id, request)
        if (!updated) {
            throw ApiException("Location not found", HttpStatusCode.NotFound)
        }

        return getLocationById(id)
    }

    suspend fun deleteLocation(id: Int) {
        val deleted = locationRepository.delete(id)
        if (!deleted) {
            // Check if location exists
            getLocationById(id) // This will throw if not found
            // If we reach here, location exists but has children
            throw ApiException("Cannot delete location that has child locations", HttpStatusCode.BadRequest)
        }
    }

    suspend fun searchLocations(searchTerm: String): List<LocationDto> {
        if (searchTerm.isBlank()) {
            throw ApiException("Search term cannot be empty", HttpStatusCode.BadRequest)
        }

        return locationRepository.searchByName(searchTerm)
    }

    suspend fun getLocationHierarchy(): List<LocationHierarchyDto> {
        return locationRepository.getLocationHierarchy()
    }

    private suspend fun validateLocationRequest(name: String, parentLocationId: Int?) {
        when {
            name.isBlank() -> throw ApiException("Location name cannot be empty", HttpStatusCode.BadRequest)
            name.length > 255 -> throw ApiException("Location name is too long (max 255 characters)", HttpStatusCode.BadRequest)
        }

        // Validate parent location exists if provided
        if (parentLocationId != null) {
            getLocationById(parentLocationId)
        }
    }

    private suspend fun validateNoCircularReference(locationId: Int, parentLocationId: Int) {
        if (locationId == parentLocationId) {
            throw ApiException("Location cannot be its own parent", HttpStatusCode.BadRequest)
        }

        // Check if parentLocationId is a descendant of locationId
        var currentParentId: Int? = parentLocationId
        while (currentParentId != null) {
            if (currentParentId == locationId) {
                throw ApiException("Circular reference detected - parent cannot be a descendant", HttpStatusCode.BadRequest)
            }

            val parentLocation = locationRepository.findById(currentParentId)
            currentParentId = parentLocation?.parentLocationId
        }
    }

    suspend fun validateLocationExists(id: Int) {
        if (!locationRepository.exists(id)) {
            throw ApiException("Location not found", HttpStatusCode.NotFound)
        }
    }
}