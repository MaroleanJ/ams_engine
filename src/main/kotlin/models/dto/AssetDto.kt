package com.techbros.models.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant

@Serializable
data class AssetDto(
    val id: Int? = null,
    val categoryId: Int,
    val vendorId: Int? = null,
    val locationId: Int? = null,
    val name: String,
    val model: String? = null,
    val serialNumber: String? = null,
    val barcode: String? = null,
    val purchaseDate: String? = null, // ISO date string
    val purchasePrice: String? = null, // String representation of decimal
    val currentValue: String? = null, // String representation of decimal
    val depreciationRate: String? = null, // String representation of decimal
    val warrantyExpiry: String? = null, // ISO date string
    val assignedTo: Int? = null,
    val status: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: String? = null, // ISO timestamp string
    val updatedAt: String? = null // ISO timestamp string
)

@Serializable
data class CreateAssetRequest(
    val categoryId: Int,
    val vendorId: Int? = null,
    val locationId: Int? = null,
    val name: String,
    val model: String? = null,
    val serialNumber: String? = null,
    val barcode: String? = null,
    val purchaseDate: String? = null, // ISO date string
    val purchasePrice: String? = null, // String representation of decimal
    val currentValue: String? = null, // String representation of decimal
    val depreciationRate: String? = null, // String representation of decimal
    val warrantyExpiry: String? = null, // ISO date string
    val assignedTo: Int? = null,
    val status: String? = null,
    val location: String? = null,
    val notes: String? = null
)

@Serializable
data class UpdateAssetRequest(
    val categoryId: Int,
    val vendorId: Int? = null,
    val locationId: Int? = null,
    val name: String,
    val model: String? = null,
    val serialNumber: String? = null,
    val barcode: String? = null,
    val purchaseDate: String? = null, // ISO date string
    val purchasePrice: String? = null, // String representation of decimal
    val currentValue: String? = null, // String representation of decimal
    val depreciationRate: String? = null, // String representation of decimal
    val warrantyExpiry: String? = null, // ISO date string
    val assignedTo: Int? = null,
    val status: String? = null,
    val location: String? = null,
    val notes: String? = null
)

@Serializable
data class AssetSummaryDto(
    val id: Int,
    val name: String,
    val model: String? = null,
    val serialNumber: String? = null,
    val status: String? = null,
    val categoryId: Int,
    val vendorId: Int? = null,
    val assignedTo: Int? = null,
    val purchasePrice: String? = null,
    val currentValue: String? = null,
    val hasWarranty: Boolean
)

@Serializable
data class AssetDetailDto(
    val id: Int,
    val categoryId: Int,
    val categoryName: String? = null,
    val vendorId: Int? = null,
    val vendorName: String? = null,
    val locationId: Int? = null,
    val locationName: String? = null,
    val name: String,
    val model: String? = null,
    val serialNumber: String? = null,
    val barcode: String? = null,
    val purchaseDate: String? = null,
    val purchasePrice: String? = null,
    val currentValue: String? = null,
    val depreciationRate: String? = null,
    val warrantyExpiry: String? = null,
    val assignedTo: Int? = null,
    val assignedToName: String? = null,
    val status: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)