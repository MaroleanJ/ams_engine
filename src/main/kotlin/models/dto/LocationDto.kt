package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null,
    val createdAt: String
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
data class LocationWithChildrenDto(
    val id: Int? = null,
    val name: String,
    val description: String? = null,
    val address: String? = null,
    val parentLocationId: Int? = null,
    val createdAt: String,
    val children: List<LocationDto> = emptyList()
)