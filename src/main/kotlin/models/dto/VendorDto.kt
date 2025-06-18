package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class VendorDto(
    val id: Int? = null,
    val name: String,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null
)

@Serializable
data class CreateVendorRequest(
    val name: String,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null
)

@Serializable
data class UpdateVendorRequest(
    val name: String,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val address: String? = null
)

@Serializable
data class VendorSummaryDto(
    val id: Int,
    val name: String,
    val contactEmail: String? = null,
    val hasPhone: Boolean,
    val hasAddress: Boolean
)