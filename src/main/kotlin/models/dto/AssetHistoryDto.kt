package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssetHistoryDto(
    val id: Int? = null,
    val assetId: Int,
    val changedBy: Int,
    val changedByName: String? = null, // For display purposes
    val actionType: String,
    val fieldChanged: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val description: String? = null,
    val changedAt: String // ISO format timestamp
)

@Serializable
data class CreateAssetHistoryRequest(
    val assetId: Int,
    val changedBy: Int,
    val actionType: String,
    val fieldChanged: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateAssetHistoryRequest(
    val actionType: String,
    val fieldChanged: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val description: String? = null
)