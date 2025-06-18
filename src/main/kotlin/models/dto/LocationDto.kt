package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null,
    val parentLocationName: String? = null, // For easier display
    val createdAt: String? = null // Format: ISO 8601
)

@Serializable
data class CreateLocationRequest(
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null
)

@Serializable
data class UpdateLocationRequest(
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null
)

@Serializable
data class LocationHierarchyDto(
    val id: Int,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null,
    val children: List<LocationHierarchyDto> = emptyList(),
    val createdAt: String
)